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
import java.lang.Math;
import java.util.Iterator;

public class NextMove extends DefaultInternalAction
{
    static Logger logger = Logger.getLogger(NextMove.class.getName());
    Term searching  = Literal.parseLiteral("searching");
    Term running    = Literal.parseLiteral("running");
    Term sneaking    = Literal.parseLiteral("sneaking");
    Term hiding    = Literal.parseLiteral("hiding");


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
            if (!ts.getAg().believes(Literal.parseLiteral("myPos(X, Y)"), u) )
            {
                throw new JasonException("Missing belief \"myPos\" ");
            }
            if (!ts.getAg().believes(Literal.parseLiteral("goal(GX, GY)"), u) )
            {
                throw new JasonException("Missing belief \"goal\" ");
            }
            int x = (int)((NumberTerm) u.get("X")).solve();
            int y = (int)((NumberTerm) u.get("Y")).solve();
            Location pos = new Location(x, y);
            
            int gx = (int)((NumberTerm) u.get("GX")).solve();
            int gy = (int)((NumberTerm) u.get("GY")).solve();
            Location goal = new Location(gx, gy);
            
            
            logger.info(": " + goal.toString());
            AStar path = new AStar(pos, goal);

            return new Iterator<Unifier>()
            {
                public boolean hasNext()
                {
                    return !path.isEmpty();
                }

                public Unifier next()
                {
                    Direction dir = path.getNext();
                    Unifier ret = un.clone();
                    // logger.info(dir.name()); // TEST
                    ret.unifies(args[0], Literal.parseLiteral(dir.name().toLowerCase()) );
                    return ret;
                }
            };
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new JasonException("The internal action 'NextMove' has not received the required argument.");
        }
        catch(Exception e)
        {
            throw new JasonException("Error in internal action 'NextMove': " + e, e);
        }
    }
}