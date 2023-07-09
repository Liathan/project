package helper;

import env.Direction;
import env.Model;
import env.Playground;
import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.Deque;
import java.util.logging.Logger;


public class NextMoveSeek extends DefaultInternalAction
{
    static Logger logger = Logger.getLogger(NextMoveSeek.class.getName());
    Term searchingLit  = Literal.parseLiteral("searching");
    Term running    = Literal.parseLiteral("running");
    boolean searching = true; // Mi dice se sta cercando o se sta correndo.
    AStar path = null;
    int lookEvery = 4;
    int moveNum = 0;

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception 
    {
        try
        {
            if (!args[0].isVar())
            {
                throw new JasonException("The argument must be a variable.");
            }
            Unifier u = new Unifier();
            if (!ts.getAg().believes(Literal.parseLiteral("state(S)"), u) )
            {
                throw new JasonException("Missing belief \"state\" ");
            }
            Term state = u.get("S");
            boolean newPath = false;
            
            //Se il cercatore ha cambiato stato in questa chiamata, devo pulire il percorso da fare
            if((state.equals(running) && searching) || (state.equals(searchingLit) && !searching))
            {
                searching = !searching;
                newPath = true;
            }

            if(newPath || path == null || path.isEmpty())
            {                
                if (!ts.getAg().believes(Literal.parseLiteral("myPos(X, Y)"), u) )
                {
                    throw new JasonException("Missing belief \"myPos\" ");
                }
                int x = (int)((NumberTerm) u.get("X")).solve();
                int y = (int)((NumberTerm) u.get("Y")).solve();
                Location pos = new Location(x, y);
                Location goal;
                if(searching)
                    goal = Model.get().freePos(Playground.OBSTACLE);
                else
                    goal = new Location(5, 5);//MAGIC NUMBER
                logger.info(goal.toString()); 
                path = new AStar(pos, goal); // Scelgo una cella casuale e ci vado
            }
            if(searching && moveNum % lookEvery == 0)
                un.unifies(args[0], Literal.parseLiteral("lookAround"));
            else
                un.unifies(args[0], Literal.parseLiteral("move("+path.getNext().name().toLowerCase()+")"));
            moveNum++;
            return true;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new JasonException("The internal action 'NextMoveSeek' has not received the required argument.");
        }
        catch(Exception e)
        {
            throw new JasonException("Error in internal action 'NextMoveSeek': " + e, e);
        }
    }
}