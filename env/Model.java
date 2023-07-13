package env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.logging.Logger;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Model extends GridWorldModel
{

    private int numAg;
    private Location[] walls = null;
    private Random rnd;
 
    public static Model model = null;
    static Logger logger = Logger.getLogger(Model.class.getName());

    public int countdown = 8;
    public Direction[] lastMovements;
    public Location home;
    public boolean counting = true;

    private Location[] createWalls()
    {
        if(walls != null)
            return walls;
        walls = new Location[27];
        walls[0]  = new Location(0, 6);
        walls[1]  = new Location(1, 2);
        walls[2]  = new Location(9, 7);
        walls[3]  = new Location(2, 1);
        walls[4]  = new Location(2, 2);
        walls[5]  = new Location(2, 4);
        walls[6]  = new Location(2, 5);
        walls[7]  = new Location(2, 6);
        walls[8]  = new Location(3, 1);
        walls[9]  = new Location(3, 2);
        walls[10] = new Location(3, 6);
        walls[11] = new Location(3, 8);
        walls[12] = new Location(3, 9);
        walls[13] = new Location(4, 3);
        walls[14] = new Location(6, 0);
        walls[15] = new Location(6, 2);
        walls[16] = new Location(6, 3);
        walls[17] = new Location(6, 6);
        walls[18] = new Location(6, 7);
        walls[19] = new Location(7, 3);
        walls[20] = new Location(7, 6);
        walls[21] = new Location(7, 7);
        walls[22] = new Location(7, 9);
        walls[23] = new Location(8, 1);
        walls[24] = new Location(8, 3);
        walls[25] = new Location(8, 9);
        walls[26] = new Location(9, 3);
        return walls;
    }

    public static Model get()
    {
        return model;
    }

    public Model()
    {
        super(Playground.GSize, Playground.GSize, Playground.NUMAG);
        model = this;
        rnd = new Random();
        numAg = Playground.NUMAG;
        lastMovements = new Direction[numAg];
        for(int i = 0; i < numAg; ++i)
        {
            lastMovements[i] = Direction.values()[rnd.nextInt(numAg)];
        }
        home = new Location(5, 5); //MAGIC NUMBER
        setAgPos(0, 5, 5); //MAGIC NUMBER
        for(Location l : createWalls()) // TODO: Randomizzare il "labirinto"? problema: probabilmente non è connesso
        {
            add(OBSTACLE, l);
        }
        for(int i = 1; i < numAg; ++i)
        {           
            Location l = getFreePos(AGENT | OBSTACLE);
            setAgPos(i, l);
        }
        add(Playground.HOME, home);
    
        // Test
        // Location l = getFreePos(AGENT | OBSTACLE);
        // setAgPos(0, 7, 2);
        // setAgPos(1, 7, 7);
        // setAgPos(2, 4, 4);
    }

    public Location freePos(int tmp)
    {
        return getFreePos(tmp);
    }

    public boolean move(Direction dir, int ag)
    {
        Location l = getAgPos(ag);
        Location nextL = Direction.nextInDir(l, dir);
        // TODO: con questa condizione se un agente vuole andare nella casella in cui c'è un altro agente che si sta spostando
        // dipende dallo scheduler se può o no. Ha senso evitarlo? se sì come?   
        if(nextL != null && isFree(AGENT | OBSTACLE, nextL))
        {
            setAgPos(ag, nextL);
            lastMovements[ag] = dir;
            return true;
        }
        return false;
    }

    public boolean count()
    {
        countdown--;
        if(countdown < 0)
            counting = false;
        return counting;
    }

    private ArrayList<Location> vision(int ag, Direction dir)
    {
        ArrayList<Location> ret = new ArrayList<Location>();
        Location l = getAgPos(ag);
        int x = l.x, y = l.y;
        // TODO: Vede anche dietro entro una casella
        // Forse ha senso cambiare e fare che veda solo a fianco e davanti
        for(int i = -1; i < 2; ++i)
        {
            for(int j = -1; j < 2; ++j)
            {
                if(i == 0 && j == 0)
                    continue;
                if(inGrid(x + i, y + j) && hasObject(AGENT, x + i, y + j))
                {
                    ret.add(new Location(x + i, y +j));
                }
            }
        }

        l = Direction.nextInDir(l, dir); // Posizione dell'agente stesso, la salto
        while(inGrid(l) && !hasObject(OBSTACLE, l))
        {
            if(hasObject(AGENT, l))
            {
                ret.add( l );
            }
            l = Direction.nextInDir(l, dir);
        }
        return ret;
    }

    public ArrayList<Location> vision(int ag)
    {
        Direction dir = lastMovements[ag];
        // A volte lo scheduler fa in modo che venga vision venga chiamato su un agente che non esiste più (liberato/trovato), quindi aggiungo questo controllo
        if(dir == null)
            return new ArrayList<Location>();
        return vision(ag, dir);
    }

    public ArrayList<Location> lookAround(int agId) 
    {
        Direction dir = lastMovements[agId];
        ArrayList<Location> ret = new ArrayList<Location>();
        for(Direction d : Direction.adjDir180(dir))
        {
            ret.addAll(vision(agId, d));
        }
        return ret;
    }

    // Restituisce la direzione in cui posso fare la peek, null se non posso
    public Direction peekDir(Location pos, Direction dir) // questa dir deve essere una delle direzioni diagonali
    {
        // Se nella direzione in cui voglio guardare c'è un ostacolo, non posso
        // Location pos = getAgPos(agId);
        if(hasObject(OBSTACLE, Direction.nextInDir(pos, dir)))
            return null;

        Direction[] values = Direction.values();
        Direction orto1 = values[(dir.ordinal() + 1) % 8];
        Direction orto2 = values[(dir.ordinal() + 7) % 8]; // 7 è congruo a -1 modulo 8, l'operatore % da un numero negativo se il primo operando è negativo.
        boolean obstacle1 = hasObject(OBSTACLE, Direction.nextInDir(pos, orto1));
        boolean obstacle2 = hasObject(OBSTACLE, Direction.nextInDir(pos, orto2));

        if( obstacle1 && !obstacle2)
        {
            return orto1;
        }
        if(!obstacle1 && obstacle2)
        {
            return orto2;
        }
        return null;
    }

    public ArrayList<Location> peek(int agId) throws Exception
    {
        Direction dir = lastMovements[agId];
        Direction[] adj = Direction.adjDir180(dir);
        Location pos = getAgPos(agId);
        ArrayList<Location> ret = new ArrayList<Location>();

        Direction peekDir1 = peekDir(pos, adj[1]); // le direzioni diagonali
        Direction peekDir2 = peekDir(pos, adj[3]);
        if(peekDir1 != null)
        {
            Direction see;
            Location tmp;
            // controllo dove è il muro: quella è la direzione in cui guardo
            if(hasObject(OBSTACLE, Direction.nextInDir(pos, adj[0]))) 
            {
                see = adj[0];
                tmp = Direction.nextInDir(pos, adj[2]); // sposto la visuale come se fosse nella cella successiva senza muro
            }
            else if(hasObject(OBSTACLE, Direction.nextInDir(pos, adj[2])))
            {
                see = adj[2];
                tmp = Direction.nextInDir(pos, adj[0]);
            }
            else
                throw new Exception("PEEK: Errore che non dovrebbe mai succedere"); // se fallisce almeno me ne accorgo subito;
            
            while(inGrid(tmp) && !hasObject(OBSTACLE, tmp))
            {
                if(hasObject(AGENT, tmp))
                {
                    ret.add( tmp );
                }
                tmp = Direction.nextInDir(tmp, see);
            }
        }
        // TODO: Rifattorizzare in un metodo?
        if(peekDir2 != null)
        {
            Direction see;
            Location tmp;
            // controllo dove è il muro: quella è la direzione in cui guardo
            if(hasObject(OBSTACLE, Direction.nextInDir(pos, adj[4]))) 
            {
                see = adj[4];
                tmp = Direction.nextInDir(pos, adj[2]); // sposto la visuale come se fosse nella cella successiva senza muro
            }
            else if(hasObject(OBSTACLE, Direction.nextInDir(pos, adj[2])))
            {
                see = adj[2];
                tmp = Direction.nextInDir(pos, adj[4]);
            }
            else
                throw new Exception("PEEK: Errore che non dovrebbe mai succedere"); // se fallisce almeno me ne accorgo subito;
            
            while(inGrid(tmp) && !hasObject(OBSTACLE, tmp))
            {
                if(hasObject(AGENT, tmp))
                {
                    ret.add( tmp );
                }
                tmp = Direction.nextInDir(tmp, see);
            }
        }
        
        return ret;
    }

    public boolean die(int agId)
    {
        remove(AGENT, getAgPos(agId));
        Playground.liveAgents.remove((Integer) agId);
        lastMovements[agId] = null;
        return true;
    }
}