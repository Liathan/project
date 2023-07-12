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

public class RandomCell extends DefaultInternalAction
{
    static Logger logger = Logger.getLogger(RandomCell.class.getName());

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception 
    {
        try{
            if (!args[0].isVar() && !args[1].isVar())
            {
                throw new JasonException("The 2 arguments must be variables.");
            }
            Location pos = Model.get().freePos(Playground.OBSTACLE);
            un.unifies(args[0], new NumberTermImpl(pos.x));
            un.unifies(args[1], new NumberTermImpl(pos.y));
            return true;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new JasonException("The internal action 'RandomCell' has not received the required argument.");
        }
        catch(Exception e)
        {
            throw new JasonException("Error in internal action 'RandomCell': " + e, e);
        }
    }
}