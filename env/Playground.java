package env;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.TimeSteppedEnvironment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

// Posso provare a farlo TimeStepped, casomai
public class Playground extends Environment {

    public static final int GSize = 11; // TODO: Ingrandire la griglia se decido di randomizzare l'ambiente
    public static final int OBSTACLE = 4;
    public static final int HOME = 8;
    public static final int NUMAG = 5;
    public static ConcurrentLinkedQueue<Integer> liveAgents;

    static Logger logger = Logger.getLogger(Playground.class.getName());

    private Model model;
    private View view;

    Term up    = Literal.parseLiteral("move(up)");
    Term down  = Literal.parseLiteral("move(down)");
    Term right = Literal.parseLiteral("move(right)");
    Term left  = Literal.parseLiteral("move(left)");
    Term count  = Literal.parseLiteral("count");
    Term lookAround  = Literal.parseLiteral("lookAround");
    Term peek  = Literal.parseLiteral("peek");
    Term die  = Literal.parseLiteral("die");
    
    @Override
    public void init(String[] args) 
    {
        model = new Model();
        view = new View(model);
        liveAgents = new ConcurrentLinkedQueue<Integer>();
        for(int i = 0; i < NUMAG; ++i)
            liveAgents.add(i);
        model.setView(view);
        updatePercepts();   
    }
    
    public String id2Name(int id)
    {
        return id == 0 ? "seeker" : "hideAg"+id;
    }

    public int name2Id(String name)
    {
        return name.equals("seeker") ?  0 : Integer.parseInt(name.substring(6));
    }

    private void updatePercepts()
    {
        clearPercepts();
        
        for(Integer i : liveAgents) // Gli agenti trovati non devono più essere aggiornati
        {
            Location l = model.getAgPos(i);
            Literal pos = Literal.parseLiteral("myPos(" +l.x+","+l.y+" )");
            String name = id2Name(i);
            clearPercepts(name);
            addPercept(name, pos);

            if(i == 0 && model.counting) // il cercatore non deve vedere mentre conta
                continue;
            
            for(Location sawloc : model.vision(i)) //model.vision restituisce le posizioni degli agenti visti o un array vuoto
            {
                String seenName = id2Name(model.getAgAtPos(sawloc));
                String s = "pos("+ seenName +", "+sawloc.x+", "+sawloc.y+ ")";
                Literal seen = Literal.parseLiteral(s);
                addPercept(name, seen);
            }
        }
        addPercept("seeker", Literal.parseLiteral("remaining("+(liveAgents.size() - 1)+")"));
        if(model.counting)
        {
            Literal pos = Literal.parseLiteral("pos(seeker," +model.home.x+","+model.home.y+" )");
            addPercept(pos);
        }
    }

    @Override
    public boolean executeAction(String ag, Structure action) 
    {
        // logger.info(ag+" doing: "+action);
        int agId = name2Id(ag);
        boolean result = false;
        try 
        {
            // TODO: potrebbe aver senso usare il functor per avere una variabile con la direzione (?)
            if (action.equals(up))
                result = model.move(Direction.UP, agId);
            else if (action.equals(down))
                result = model.move(Direction.DOWN, agId);
            else if (action.equals(right))
                result = model.move(Direction.RIGHT, agId);
            else if (action.equals(left))
                result = model.move(Direction.LEFT, agId);
            else if (action.equals(count))
                result = model.count();
            else if (action.equals(die))
                result = model.die(agId);
            else if (action.equals(lookAround))
            {
                // logger.info("-----------------"+model.canPeek(1, Direction.DOWN_LEFT)); //TEST

                ArrayList<Location> l = model.lookAround(agId);
                for(Location sawloc : l)
                {
                    String seenName = id2Name(model.getAgAtPos(sawloc));
                    String s = "pos("+ seenName +", "+sawloc.x+", "+sawloc.y+ ")";
                    Literal seen = Literal.parseLiteral(s);
                    addPercept(ag, seen);
                }
                informAgsEnvironmentChanged(ag);
                result = true;
            }
            else if(action.equals(peek))
            {
                ArrayList<Location> l = model.peek(agId);
                for(Location sawloc: l)
                {
                    // logger.info(sawloc.toString()); // TEST
                    String seenName = id2Name(model.getAgAtPos(sawloc));
                    String s = "pos("+ seenName +", "+sawloc.x+", "+sawloc.y+ ")";
                    Literal seen = Literal.parseLiteral(s);
                    addPercept(ag, seen);
                }
                informAgsEnvironmentChanged(ag);
                result = true;
            }
            else
            {
                logger.warning("executing: " + action + ", but not implemented!");
            }
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        updatePercepts();
        try {
            Thread.sleep(300);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return result;

    }   

    
}