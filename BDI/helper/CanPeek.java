package helper;

import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;
import env.Direction;
import jason.environment.grid.Location;
import env.Model;

public class CanPeek extends DefaultInternalAction
{
    Model model;

    public CanPeek()
    {
        model = Model.get();
    }

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception 
    {

        Unifier u = new Unifier();
        if (!ts.getAg().believes(Literal.parseLiteral("myPos(X, Y)"), u) )
        {
            throw new JasonException("Missing belief \"myPos\" ");
        }
        
        int x = (int)((NumberTerm) u.get("X")).solve();
        int y = (int)((NumberTerm) u.get("Y")).solve();
        Location pos = new Location(x, y);
        Direction dir = model.lastMovements[model.getAgAtPos(pos)];
        if(dir == null)
            return false;
        Direction[] adj = Direction.adjDir180(dir);
        return (model.peekDir(pos, adj[1]) != null || model.peekDir(pos, adj[3]) != null);
    }
}