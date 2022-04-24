package hu.kovacsde.graphtestguava;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class LineView extends View {

    //Grafikai beállításokhoz osztálypéldány
    private final Paint paint = new Paint();
    //Színek a rajzoláshoz
    private final int basicColor = getResources().getColor(R.color.purple_200);
    private final int routeColor = getResources().getColor(R.color.red_500);

    //Élek listája
    public List<Edge> edges = new ArrayList<>();


    //Konstruktorok
    public LineView(Context context) {
        super(context);
    }

    public LineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //Él kirajzolása
    @Override
    protected void onDraw(Canvas canvas) {
        //Vonalvastagság beállítása
        int strokeWidth = 10;
        paint.setStrokeWidth(strokeWidth);

        for (Edge l :
                edges) {
            //Beállítjuk az él színét aszerint, hogy része-e az útvonalnak
            paint.setColor(l.isRoute()?routeColor:basicColor);

            //Hozzáadunk egy vonalat a két végpont (csúcsok) között
            canvas.drawLine(l.getStart().getPoint().x, l.getStart().getPoint().y, l.getEnd().getPoint().x, l.getEnd().getPoint().y, paint);

            //Hozzáadunk egy-egy kört a csúcspontok helyére
            canvas.drawCircle(l.getStart().getPoint().x,l.getStart().getPoint().y,strokeWidth*2 ,paint);
            canvas.drawCircle(l.getEnd().getPoint().x,l.getEnd().getPoint().y,strokeWidth*2 ,paint);

            //Kirajzoljuk az alakzatot
            l.Draw(getContext(), MainActivity.graphView);
        }

        super.onDraw(canvas);
    }

    //Frissítjük a nézetet
    public void update(){
        invalidate();
        requestLayout();
    }

    //Visszaadja a két csúcs között futó élet
    public Edge edgeBetween(Node u, Node v){
        Edge edge = new Edge(u, v);

        //Ha létezik ilyen él, akkor keressük meg és adjuk vissza
        if(edges.contains(edge)){
            for (Edge e :
                    edges) {
                if (e.equals(edge)){
                    return e;
                }
            }
        }

        //Ha nem volt ilyen él, akkor adjunk vissza null értéket
        return null;
    }

    //Visszaadja a kapcsolódó éleket
    public ArrayList<Edge> connectingEdges(Node node){
        ArrayList<Edge> connecting = new ArrayList<>();
        //Minden listában lévő élet ellenőrzünk...
        for (Edge e :
                edges) {
            //Ha valamely pontja megegyezik a paraméterben lévő csúccsal,
            //akkor az kapcsolódik hozzá, felvesszük a listába
            if(e.getStart().equals(node) || e.getEnd().equals(node))
                connecting.add(e);
        }

        //A listát visszaadjuk
        return connecting;
    }
}
