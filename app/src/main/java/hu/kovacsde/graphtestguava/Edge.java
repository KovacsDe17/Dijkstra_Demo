package hu.kovacsde.graphtestguava;

import static hu.kovacsde.graphtestguava.MainActivity.graph;
import static hu.kovacsde.graphtestguava.MainActivity.lineView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class Edge {

    private final Node start; //Kezdőpont csúcs
    private final Node end;   //Végpont csúcs
    private int value = 0;  //Az él költsége
    private boolean route = false;  //Logikai változó: része-e az él az aktuális útvonalnak

    //Konstruktorok
    public Edge(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    public Edge(Node start, Node end, int value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isRoute() {
        return route;
    }

    public void setRoute(boolean route) {
        this.route = route;
    }

    //Kirajzolja az élet a megadott konténerben
    public void Draw(Context context, ViewGroup layout){

        //Ellenőrizzük, hogy ki van-e már rajzolva az él
        RelativeLayout oldHolder = layout.findViewById(this.hashCode());
        if(oldHolder!=null){
            //Ha igen, akkor töröljük az előzőt
            layout.removeView(oldHolder);
        }

        //Beállítjuk a megfelelő külsőt az élnek az alapján, hogy része-e az aktuális útvonalnak
        int layoutID = route?R.layout.value_holder_route:R.layout.value_holder;

        //Létrehozzuk a külső alapján a View objektumot
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout valueHolder = (RelativeLayout) inflater.inflate(layoutID, null, false);
        valueHolder.setId(this.hashCode());

        int size = 80;

        //Megadjuk a helyét, értékét, azonosítóját
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,size);
        params.leftMargin = (start.getPoint().x+end.getPoint().x)/2-(size/2);
        params.topMargin = (start.getPoint().y+end.getPoint().y)/2-(size/2);
        String valueString = Integer.toString(value);
        getTextView(valueHolder).setText(valueString);
        valueHolder.setId(this.hashCode());

        //A dupla érintés eseményének hatására:
        valueHolder.setOnClickListener(new Gestures.DoubleClickListener() {
            @Override
            public void OnSingleClick() {
                //Kiírjuk Toast-ban az infókat
                Info.ShowToast(context, DisplayMessage(), Toast.LENGTH_LONG);
            }

            @Override
            public void OnDoubleClick() {
                //Rezzen a készülék
                Gestures.Vibrate(10);

                //Behozzuk a szerkesztés párbeszédablakát
                CustomDialog.EditEdge(context,Edge.this);
            }
        });

        //Hozzáadjuk a konténerhez az élet
        layout.addView(valueHolder, params);
    }

    public void Delete(ViewGroup layout){
        //Ellenőrizzük, hogy ki van-e már rajzolva az él
        RelativeLayout oldHolder = layout.findViewById(this.hashCode());
        if(oldHolder!=null){
            //Ha igen, akkor töröljük az előzőt
            layout.removeView(oldHolder);
        }

        //Töröljük az élet a tartalmazó nézetből és a gráfból
        lineView.edges.remove(Edge.this);
        graph.removeEdge(Edge.this.getStart(),Edge.this.getEnd());

        //Ha futott már a Dijkstra, akkor újra lefuttatjuk ugyanarra a kezdőcsúcsra
        if(Dijkstra.getRanDijkstra())
            Dijkstra.Run(graph,Dijkstra.getLastStarter());

        //Frissítjük a tartalmazó nézetet
        lineView.update();
    }

    public void Add(){

        graph.putEdgeValue(start,end,value);
        lineView.edges.add(this);

        lineView.update();
    }

    //Visszaadjuk az élhez tartozó TextView objektumot
    private TextView getTextView(RelativeLayout holder){
        int count = holder.getChildCount();
        for (int i = 0; i < count; i++) {
            //Végignézzük az él nézetének a gyerekeit, és visszaadjuk az első TextView típusút
            if(holder.getChildAt(i) instanceof TextView)
                return (TextView) holder.getChildAt(i);
        }

        //Ha nem találtunk ilyet, akkor null értéket adunk vissza
        return null;
    }

    //Egyenlőség operátor
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        //Akkor egyenlő két él, ha azonos csúcsok között futnak
        return (Objects.equals(start, edge.start) &&
                Objects.equals(end, edge.end)) ||
                (Objects.equals(start, edge.end) &&
                Objects.equals(end, edge.start));
    }

    //Kiírja az információkat Toast-ban
    private String DisplayMessage(){
        //Az alap szöveg a költséget és a csúcsokat mutatja
        return start + " - " + end + " (" + value + ")";
    }

    public static boolean isInt(String string){
        try{
            Integer.parseInt(string);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    @Override
    public String toString() {
        return "Edge[" +
                start.toString() +
                ", " +
                end.toString() +
                "] = " +
                value;
    }
}