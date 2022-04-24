package hu.kovacsde.graphtestguava;

import static hu.kovacsde.graphtestguava.Gestures.AttachViewDragListener;
import static hu.kovacsde.graphtestguava.MainActivity.graph;
import static hu.kovacsde.graphtestguava.MainActivity.graphView;
import static hu.kovacsde.graphtestguava.MainActivity.instance;
import static hu.kovacsde.graphtestguava.MainActivity.lineView;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.graph.MutableValueGraph;

import java.util.Objects;
import java.util.Random;

public class Node { //TODO: View-vá varázsolni(?)
    //Számláló az azonosítókhoz, nevekhez (65-90 közé esnek a betűk (A-Z), ezért ehhez majd hozzá kell adni!)
    private static int COUNT = 0;
    //Az osztályhoz tartozó Toast, erre kerül a csúcsok információja
    private static Toast INFO;
    //Lehetséges csúcs-típusok (Alap, Kezdő, Útvonal)
    enum NodeType {BASIC, START, ROUTE}

    private int id;     //Azonosító
    private String name;    //Név
    private Point point;    //Pozíziója a képernyőn
    private int sumDist = 0;    //A kezdőcsúcsból történő elérés legkisebb költsége
    private Node prevoius = null;   //A megelőző csúcs
    private NodeType type = NodeType.BASIC;     //A csúcs típusa

    //Konstruktor automatikus névadással
    public Node(){
        this.name = addName(COUNT++);
        this.id = COUNT;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPoint() {
        return point;
    }

    public int getSumDist() {
        return sumDist;
    }

    public void setSumDist(int sumDist) {
        this.sumDist = sumDist;
    }

    public void setPrevoius(Node prevoius) {
        this.prevoius = prevoius;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    //A megelőző csúccsal együtt adja vissza a csúcs nevét
    public String toStringWithPrev() {
        return this.name + ((this.prevoius!=null)?(" <- " + prevoius.toStringWithPrev()):(""));
    }

    //Egyenlőség operátor
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        //Akkor egyenlő két csúcs, ha megegyezik az azonosítójuk vagy a nevük
        return (Objects.equals(id, node.id) || Objects.equals(name, node.name));
    }

    //Visszaadjuk a csúcshoz tartozó TextView objektumot
    private TextView getTextView(RelativeLayout node){
        int count = node.getChildCount();
        //Végignézzük a csúcs nézetének a gyerekeit, és visszaadjuk az első TextView típusút
        for (int i = 0; i < count; i++) {
            if(node.getChildAt(i) instanceof TextView)
                return (TextView) node.getChildAt(i);
        }

        //Ha nem találtunk ilyet, akkor null értéket adunk vissza
        return null;
    }

    //A számláló alapján nevet adunk a csúcsnak
    private String addName(int count){
        String name = "";
        char c = (char) (count+65);
        name += c;

        return name;
    }

    //Visszaállítja a számlálót 0-ra
    public static void resetCount(){
        COUNT = 0;
    }

    //Kirajzolja a csúcsot a megadott konténerben
    public void Draw(Context context, ViewGroup layout){
        //Ellenőrizzük, hogy ki van-e már rajzolva a csúcs
        RelativeLayout oldNode = layout.findViewById(this.id);
        if(oldNode!=null){
            //Ha igen, akkor töröljük az előzőt
            layout.removeView(oldNode);
        }

        //Beállítjuk a megfelelő külsőt a csúcsnak a típusa alapján (Kezdő-, Útvonal- vagy Alapcsúcs)
        int layoutID;
        switch (this.type){
            case START: layoutID = R.layout.node_start;
                break;
            case ROUTE: layoutID = R.layout.node_end;
                break;
            default: layoutID = R.layout.node;
                break;
        }

        //Létrehozzuk a külső alapján a View objektumot
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout node = (RelativeLayout) inflater.inflate(layoutID, null, false);

        int size = 150;

        //Megadjuk a helyét, nevét, azonosítóját
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,size);
        params.leftMargin = point.x-(size/2);
        params.topMargin = point.y-(size/2);
        getTextView(node).setText(this.name);
        node.setId(this.id);

        //Csatoljuk a csúcs megragadásának listener-jét
        AttachViewDragListener(node);

        node.setOnClickListener(new Gestures.DoubleClickListener() {
            @Override
            public void OnSingleClick() {
                //Kirajzoljuk az útvonalat, ha van
                DrawRoute(context, MainActivity.graphView, graph);

                //Kiírjuk Toast-ban az infókat
                Info.ShowToast(context, DisplayMessage(context), Toast.LENGTH_LONG);
            }

            @Override
            public void OnDoubleClick() {
                //Rezzen a készülék
                Gestures.Vibrate(10);

                //Behozzuk a szerkesztés párbeszédablakát
                CustomDialog.EditNode(context, Node.this);
            }
        });

        //Hozzáadjuk a konténerhez a csúcsot
        layout.addView(node, params);
    }

    //Csúcs törlése
    public void Delete(ViewGroup layout){
        //Ellenőrizzük, hogy ki van-e már rajzolva a csúcs
        RelativeLayout oldNode = layout.findViewById(this.id);
        if(oldNode!=null){
            //Ha igen, akkor töröljük az előzőt
            layout.removeView(oldNode);
        }

        //Töröljük az összes csatlakozó élet
        for (Edge edge :
                lineView.connectingEdges(this)) {
            edge.Delete(graphView);
        }

        //Töröljük a csúcsot a gráfból
        graph.removeNode(this);
    }

    //Csúcs hozzáadása
    public void Add(Context context, ViewGroup layout){
        //Lekérjük a képernyő méreteit
        DisplayMetrics dm = new DisplayMetrics();
        instance.getWindowManager().getDefaultDisplay().getMetrics(dm);

        //Koordinátákat generálunk a csúcsoknak
        int x = (new Random().nextInt(4) * (dm.widthPixels)/5)+100;
        int y = (new Random().nextInt(6) * (dm.heightPixels)/7)+100;

        //Felvesszük az új csúcsokat a gráfba és kirajzoljuk őket a képernyőre
        this.point = new Point(x,y);
        graph.addNode(this);
        this.Draw(context, layout);
    }

    //Kiírja az információkat Toast-ban
    private String DisplayMessage(Context context){
        //Az alap szöveg a költséget és az utat mutatja a két csúcs között
        String infoString = context.getString(R.string.costIs) + " " + sumDist + " (" + toStringWithPrev() + ")";

        if(prevoius == null){
            //Ha nem volt megelőző csúcs, akkor ide nem vezet út, tehát ezt írjuk ki
            infoString = context.getString(R.string.noRoute);
        }

        if(!Dijkstra.getRanDijkstra()){
            //Ha még nem futtattuk le a Dijkstra algoritmust, akkor csak a csúcs nevét írjuk ki
            infoString = context.getString(R.string.node) + " " + toString();
        }

        return infoString;
    }

    //Kirajzoljuk az útvonalat a kezdő és a kiválasztott csúcs között
    public void DrawRoute(Context context, ViewGroup viewGroup, MutableValueGraph<Node, Integer> graph){
        //Minden élnek, aminek a típusa nem Kezdő, annak visszaállítjuk a típusát Alapra, majd kirajzoljuk
        for (Node n :
                graph.nodes()) {
            if(n.type!=NodeType.START){
                n.type = NodeType.BASIC;
                n.Draw(context, viewGroup);
            }
        }

        //Minden élre beállítjuk, hogy jelenleg nem része az útvonalnak, majd kirajzoljuk eszerint
        for(Edge e :
            lineView.edges){
            e.setRoute(false);
            e.Draw(context, viewGroup);
        }

        //Ha nem volt megelőző csúcs, akkor frissítjük az élek nézetét
        if(this.prevoius==null){
            lineView.update();
            return;
        }

        //Az útvonalban legutolsó csúcsnak jelöljük a jelenlegit
        Node last = this;

        do {
            //Beállítjuk a csúcsot útvonal típusra, majd kirajzoljuk
            last.type = NodeType.ROUTE;
            last.Draw(context, viewGroup);

            //Ha van él a jelenlegi és a megelőző csúcs között, akkor bevesszük az útvonalba, majd kirajzoljuk azt
            Edge edge = lineView.edgeBetween(last, last.prevoius);
            if(edge!=null){
                edge.setRoute(true);
                edge.Draw(context, viewGroup);
            }

            //Az útvonalban legutolsó csúcsnak jelöljük a megelőzőt
            last = last.prevoius;

            //Ezt addig ismételjük, amíg van megelőző csúcs
        } while (last.prevoius!=null);

        //Frissítjük az élek nézetét
        lineView.update();
    }

    //Visszaadjuk, hogy a View melyik csúcshoz tartozik
    public static Node FindNodeByView(View view){
        for (Node n :
                graph.nodes()) {
            //Végignézzük a gráfban lévő csúcsokat, és ha egyezik az azonosító,
            //akkor visszaadjuk a hozzá tartozó csúcsot
            if(view.getId() == n.id)
                return n;
        }

        //Ha nem volt ilyen, akkor null értéket adunk vissza
        return null;
    }

    public String getSumDistString(){
        if(sumDist == Integer.MAX_VALUE)
            return "∞";
        return Integer.toString(sumDist);
    }
}
