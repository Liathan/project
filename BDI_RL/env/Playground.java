package env;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.TimeSteppedEnvironment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import jason.mas2j.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.FileInputStream;

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
    private List<String> names;

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

        try 
        {
            jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileInputStream("bdi_rl.mas2j"));
            MAS2JProject project = parser.mas();

            this.names = new ArrayList<String>();
            // get the names from the project
            for (AgentParameters ap : project.getAgents()) {
                String agName = ap.name;
                for (int cAg = 0; cAg < ap.getNbInstances(); cAg++) {
                    String numberedAg = agName;
                    if (ap.getNbInstances() > 1) {
                    numberedAg += (cAg + 1);
                    }
                    this.names.add(numberedAg);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace(); // Non dovrebbe mai succedere
        }
        model.setView(view);
        updatePercepts();   
    }
    
    public String id2Name(int id) // TODO_BDI_RL: i nomi ora hanno anche _BDI_ e _RL_
    {
        return this.names.get(id);
    }

    public int name2Id(String name) // TODO_BDI_RL: come sopra
    {
        return this.names.indexOf(name);
    }

    private void updatePercepts()
    {
        clearPercepts();
        addPercept(Literal.parseLiteral("remaining("+(liveAgents.size() - 1)+")"));
        if(model.counting)
        {
            Literal pos = Literal.parseLiteral("pos(seeker," +model.home.x+","+model.home.y+" )");
            addPercept(pos);
        }
        
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
                int id = model.getAgAtPos(sawloc);
                if(id == -1)
                {
                    continue; //a volte lo scheduling permette che tra la fine della vision e questa chiamata l'agente si sia già spostato o liberato
                }
                // TODO_BDI_RL: se un agente _RL_ vede deve cambiare stato
                String seenName = id2Name(id);
                String s = "pos("+ seenName +", "+sawloc.x+", "+sawloc.y+ ")";
                Literal seen = Literal.parseLiteral(s);
                addPercept(name, seen);
            }
        }
    }

    @Override
    public boolean executeAction(String ag, Structure action) 
    {
        int agId = name2Id(ag);
        logger.info(ag+" doing: "+action +" ------------ Id: "+agId); // TEST
        boolean result = false;
        try 
        {
            // TODO: potrebbe aver senso usare il functor per avere una variabile con la direzione (?)
            // TODO_BDI_RL: dopo la mossa gli agenti _RL_ devono sapere se sono in uno stato in cui possono fare la peek
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
            {
                result = model.die(agId);
            }
            else if (action.equals(lookAround))
            {
                // logger.info("-----------------"+model.canPeek(1, Direction.DOWN_LEFT)); //TEST

                ArrayList<Location> l = model.lookAround(agId);
                for(Location sawloc : l)
                {
                    int id = model.getAgAtPos(sawloc);
                    if(id == -1)
                    {
                        continue; //a volte lo scheduling permette che tra la fine della vision e questa chiamata l'agente si sia già spostato o liberato
                    }
                    String seenName = id2Name(id);
                    // TODO_BDI_RL: se un agente _RL_ vede deve cambiare stato
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
                    int id = model.getAgAtPos(sawloc);
                    if(id == -1)
                    {
                        continue; //a volte lo scheduling permette che tra la fine della peek e questa chiamata l'agente si sia già spostato o liberato
                    }
                    String seenName = id2Name(id);
                    // TODO_BDI_RL: se un agente _RL_ vede deve cambiare stato
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
            // TODO_BDI_RL: assicurarsi che gli agenti RL abbiano un nuovo stato dopo ogni azione
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        updatePercepts();
        try {
            Thread.sleep(500);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return result;

    }   

    
}