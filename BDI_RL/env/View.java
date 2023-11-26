package env;

import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

class View extends GridWorldView
{
    public View(Model model)
    {
        super(model, "Hide-n-Seek", 1000);
        defaultFont = new Font("Arial", Font.BOLD, 18);
        setVisible(true);
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
        case Playground.HOME:
            drawHome(g, x, y);
            break;
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) 
    {
        String label = id == 0 ? "Seeker": "Hide"+id; // TODO_BDI_RL: aggiungere la differenziazione tra agente BDI e RL nella stringa?
        super.drawAgent(g, x, y, id == 0 ? Color.red : c, -1);
        g.setColor(Color.white);
        drawString(g, x, y, defaultFont, label);
    }

    public void drawHome(Graphics g, int x, int y)
    {
        g.setColor(Color.yellow);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.lightGray);
        g.drawLine(x * cellSizeW, y * cellSizeH, (x + 1) * cellSizeW , (y) * cellSizeH );
        g.drawLine(x * cellSizeW, (y + 1) * cellSizeH , (x) * cellSizeW , y * cellSizeH);
        g.setColor(Color.black);
        drawString(g, x, y, defaultFont, "Home");
    }
}