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

public class NextMoveHide extends DefaultInternalAction
{
    static Logger logger = Logger.getLogger(NextMoveHide.class.getName());

    private Direction getRelativeDirection(Location hiding, Location seeker)
    {
        // assumo siano diverse perchè non dovrebbe essere possibile il contrario
        int xH = hiding.x, yH = hiding.y;
        int xS = seeker.x, yS = seeker.y;

        int diffHoriz = Math.abs(xH - xS), diffVert = Math.abs(yH - yS);
        int diffH = Math.abs(xH -yH), diffS = Math.abs(xS - yS);
        int sumH = xH + yH, sumS = xS + yS;

        int diffDiagMax = Math.abs(diffH - diffS); // 0 se gli agenti sono esattamente sulla stessa diagonale "maggiore"
        int diffDiagMin = Math.abs(sumH - sumS); // 0 se gli agenti sono esattamente sulla stessa diagonale "minore"
        int diffLat = Math.min(diffHoriz, diffVert);

        if(diffLat < diffDiagMax && diffLat < diffDiagMin)
        {
            if(diffVert > diffHoriz)
                if(yH > yS)
                    return Direction.UP;
                else
                    return Direction.DOWN;
            else
                if(xH > xS)
                    return Direction.LEFT;
                else
                    return Direction.RIGHT;
        }
        else 
        {
            if(diffDiagMax < diffDiagMin) 
                if(yH > yS) // sono o sotto a destra o sopra a sininstra
                    return Direction.UP_LEFT;
                else
                    return Direction.DOWN_RIGHT;
            else
                if(yH > yS)
                    return Direction.UP_RIGHT;
                else
                    return Direction.DOWN_LEFT;
        } 
    }

    // TODO: fare in modo che non si nascondano nello stesso posto
    private Location getHidingSpot(Location pos, int xSeeker, int ySeeker)
    {
        Model model = Model.get();
        int maxMove = model.counting ? model.countdown : 3;
        // Voglio andare nella direzione opposta a quella in cui c'è il cercatore
        // Quindi cerco il nascondiglio nello stesso quadrante, ma se sono nella stessa riga/colonna, amplio con il quadrante adiacente
        int x = pos.x <= xSeeker ? 0 : xSeeker; 
        int y = pos.y <= ySeeker ? 0 : ySeeker;
        int endX = pos.x < xSeeker ? xSeeker : Playground.GSize; 
        int endY = pos.y < ySeeker ? ySeeker : Playground.GSize;
        double maxH = 0, secondMax = 0;
        Location maxLoc = null, secondLoc = null;
        for(int i = x; i < endX; ++i)
        {
            for(int j = y; j < endY; ++j)
            {
                Location tmp = new Location(i, j);
                //logger.info("Position: "+tmp.toString()); //TEST
                if( !(pos.distanceManhattan(tmp) < maxMove) || !model.isFree(Playground.OBSTACLE, tmp))
                    continue;
                // Uso un'euristica per determinare se la posizione è un nascondiglio adeguato
                // prendo in considerarione la distanza dall'ultima posizione nota del cercatore
                // e quanti muri ha intorno
                // TODO: questa euristica da sempre lo stesso valore per la stessa cella: Ha senso introdurre della casualità?
                double heuristic = tmp.distanceManhattan(new Location(xSeeker, ySeeker)) / 4.0;
                Direction relative = getRelativeDirection(tmp, new Location(xSeeker, ySeeker));
                Direction[] adj = Direction.adjDir180(relative);
                double[] mask = new double[5];
                mask[1] = 1;
                mask[2] = 2;
                mask[3] = 1;
                for(int k =1; k < 4; ++k) // I muri utili sono solo nellle direzioni -45°, 0° e 45°
                {
                    Location neighbour = Direction.nextInDir(tmp, adj[k]);
                    if(model.hasObject(Playground.OBSTACLE, neighbour))
                        heuristic+= mask[k];
                }
                // logger.info("Position: "+tmp.toString() + "Heuristic: "+heuristic + "Direction: "+ relative.name()); // TEST
                if(maxH < heuristic)
                {
                    secondMax = maxH;
                    secondLoc = maxLoc;
                    maxH = heuristic;
                    maxLoc = tmp;
                }
            }
        }
        return maxLoc;
    }

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
            int x = (int)((NumberTerm) u.get("X")).solve();
            int y = (int)((NumberTerm) u.get("Y")).solve();
            Location pos = new Location(x, y);
            
            if (!ts.getAg().believes(Literal.parseLiteral("lastSeen(A, B)"), u) )
            {
                throw new JasonException("Missing belief \"lastSeen\" ");
            }
            int xSeeker = (int) ((NumberTerm) u.get("A")).solve();
            int ySeeker = (int) ((NumberTerm) u.get("B")).solve();
            Location goal = getHidingSpot(pos, xSeeker, ySeeker); 
            logger.info("Hiding: " + goal.toString());

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
                    ret.unifies(args[0], Literal.parseLiteral(dir.name().toLowerCase()) );
                    return ret;
                }
            };
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new JasonException("The internal action 'NextMoveHide' has not received the required argument.");
        }
        catch(Exception e)
        {
            throw new JasonException("Error in internal action 'NextMoveHide': " + e, e);
        }
    }
}