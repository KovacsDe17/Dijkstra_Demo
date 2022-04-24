package hu.kovacsde.graphtestguava;

import static hu.kovacsde.graphtestguava.Gestures.nodeDragListener;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static ViewGroup graphView;
    public static LineView lineView;
    public static MutableValueGraph<Node, Integer> graph;
    public static AppCompatActivity instance;
    private Button optionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Elmentjük a MainActivity jelenlegi példányát
        if(instance == null)
            instance = this;

        //Hivatkozás a gui elemekre
        graphView = findViewById(R.id.graphView);
        lineView = findViewById(R.id.lineView);
        optionsButton = findViewById(R.id.optionsButton);

        //A gráfot tartalmazó konténerre rátesszük a csúcsok megragadásának listener-jét
        graphView.setOnDragListener(nodeDragListener);

        //Létrehozzuk a gráf objektumot
        graph = ValueGraphBuilder.undirected().build();

        //A generáló gomb érintés eseménye...
        optionsButton.setOnClickListener((View view)->{
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, optionsButton);//Felurgó menü létrehozása
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                //Akciók hozzárendelése az egyes menüpontokhoz
                switch (menuItem.getItemId()){
                    case R.id.newGraph:
                        GenerateAction();
                        break;

                    case R.id.addNode:
                        Node node = new Node();
                        node.Add(this,graphView);
                        break;

                    case R.id.addEdge:
                        CustomDialog.AddEdge(this);
                        break;

                    case R.id.runDijkstra: RunAction(); break;

                    case R.id.results: CustomDialog.Results(this); break;

                    default:
                        Info.ShowToast(instance,"Nincs erre a menüpontra akció rögzítve!", Toast.LENGTH_SHORT);
                }
                return true;
            });
            //Felugró menü mutatása
            popupMenu.show();
        });
    }

    public static void GenerateGraph(Context context, LineView lineView, ViewGroup viewGroup, MutableValueGraph<Node, Integer> graph, int nodeCount, int edgeCount){
        //Maximum ennyi él lehet egy irányítatlan gráfban
        int maxEdges = nodeCount*(nodeCount-1)/2;
        //Minimum ennyi él lehet egy összefüggő, irányítatlan gráfban
        int minEdges = nodeCount-1;

        //Ha több az él a megengedettnél, akkor azt beállítjuk a maximumra
        if(edgeCount>maxEdges){
            edgeCount = maxEdges;
            return;
        }

        //Ha kevesebb az él a megengedettnél, akkor azt beállítjuk a minimumra
        if(edgeCount<minEdges){
            edgeCount = minEdges;
            return;
        }

        //Lekérjük a képernyő méreteit
        DisplayMetrics dm = new DisplayMetrics();
        instance.getWindowManager().getDefaultDisplay().getMetrics(dm);

        //Minden felvenni kívánt csúcsra:
        for(int i=0; i<nodeCount; i++){
            Node node = new Node();
            node.Add(context,graphView);
        }

        //Lehetséges élek, ezekből fogjuk kivenni az elemeket a generálásnál
        ArrayList<Edge> possibleEdges = new ArrayList<>();
        //Minden lehetséges csúcspáron végigmegyünk
        for (Node endNode : graph.nodes()) {
            for (Node startNode : graph.nodes()) {
                //A hurokéleket és a korábban már felvett élek duplikációját elvetjük
                if(startNode==endNode || possibleEdges.contains(new Edge(startNode,endNode,0))){
                    break;
                }
                //A többi esetben pedig felvesszük az élet
                possibleEdges.add(new Edge(startNode, endNode, 1));
            }
        }

        //Minden felvenni kívánt élre:
        for (int i=0; i<edgeCount; i++){
            //Véletlenszerűen generálunk egy indexet a lehetséges élek számáig,
            //majd kiválasztjuk az ehhez tartozó élet a lehetségesek közül
            int edgeIndex = new Random().nextInt(possibleEdges.size());
            Edge edge = possibleEdges.get(edgeIndex);

            //Véletlenszerűen értéket adunk az élnek 1 és 10 között
            int edgeValue = new Random().nextInt(9)+1;
            edge.setValue(edgeValue);

            //Hozzáadjuk a gráfhoz és az élek listájához ezt az élet
            graph.putEdgeValue(edge.getStart(),edge.getEnd(),edge.getValue());
            lineView.edges.add(edge);

            //Majd a lehetséges élekből eltávolítjuk
            possibleEdges.remove(edge);
        }

        //Frissítjuk az élek nézetét
        lineView.update();
    }

    //Generálás menüpont parancsa
    private void GenerateAction(){
        //Ha nem üres a gráf, akkor megkérdezzük, hogy törölni kívánjuk-e azt
        if(graph.nodes().size()>0){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(getResources().getText(R.string.sureToDeleteGraph))
                    .setTitle(R.string.deleteGraph)
                    .setPositiveButton(getResources().getText(R.string.delete), (dialog, which) -> {
                        //Pozitív válasz esetén töröljük a gráf konténer teljes tartalmát,
                        // és visszaállítjuk a vonalak UI reprezentációjáért felelős objektumot
                        graphView.removeAllViews();
                        lineView.edges.clear();
                        lineView.update();

                        //Új gráfot hozunk létre és visszaállítjuk a csúcsok számlálóját
                        graph = ValueGraphBuilder.undirected().build();
                        Node.resetCount();

                        CustomDialog.Generate(this);
                        Dijkstra.setRanDijkstra(false);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        //Negatív válasz esetén visszalépünk
                        dialog.cancel();
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }

        //Abban az esetben, ha üres volt, akkor felugrik a generálás párbeszédablaka
        CustomDialog.Generate(this);

        //Beállítjuk, hogy a Dijkstra algoritmust még nem futtattuk le
        Dijkstra.setRanDijkstra(false);
    }

    //Futtatás menüpont parancsa
    private void RunAction(){
        //Ha nem üres a gráf
        if(graph.nodes().size()>0){
            //Megjelenítjük a Dijkstra algoritmus indításához a párbeszédablakot
            CustomDialog.Dijkstra(this);
        }
        else{
            //Ha üres, akkor jelezzük a felhasználónak, hogy még nincsen gráf, amin fusson az algoritmus
            Toast.makeText(MainActivity.graphView.getContext(), getResources().getText(R.string.firstCreateGraph), Toast.LENGTH_SHORT).show();
        }
    }
}