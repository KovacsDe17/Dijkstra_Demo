package hu.kovacsde.graphtestguava;

import static hu.kovacsde.graphtestguava.MainActivity.graph;
import static hu.kovacsde.graphtestguava.MainActivity.graphView;
import static hu.kovacsde.graphtestguava.MainActivity.lineView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.graph.MutableValueGraph;

import java.util.ArrayList;

public abstract class CustomDialog {

    //Egy él szerkesztésének dialógusa
    public static void EditEdge(Context context, Edge edge){
        //Létrehozzuk a dialógust
        final Dialog dialog = new Dialog(context);

        //Dialógus típusának és kinézetének beállítása
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_dialog);

        //UI elemek
        TextView title = dialog.findViewById(R.id.dialog_title);
        final EditText editText = dialog.findViewById(R.id.editText);
        Button submitBtn = dialog.findViewById(R.id.generate_btn);
        Button deleteBtn = dialog.findViewById(R.id.delete_button);

        //Cím beállítása aszerint, hogy melyik élet szerkesztjük
        String titleText = context.getString(R.string.edge) + " (" + edge.getStart().toString() + " - " + edge.getEnd().toString() + ")";
        title.setText(titleText);

        //Szövegmezőbe beírjuk az élköltséget
        String valueString = Integer.toString(edge.getValue());
        editText.setText(valueString);

        //Figyeljük, hogy változik-e a szövegmező
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Ha változik, akkor ellenőrizzük, hogy van-e beírva éppen szöveg,
                //és, hogy az egész szám-e
                //Eszerint tiltjuk le a szerkesztő gombot vagy sem
                submitBtn.setEnabled(s.length() > 0 && Edge.isInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Új élet hozunk létre (lambda kifejezés miatt kell egy új final típus!)
        final Edge edge2 = edge;

        //A szerkesztő gomb megnyomásának eseménye
        submitBtn.setOnClickListener((View view) -> {
            //Beállítjuk a szövegmező értékét az él költségének
            edge2.setValue(Integer.parseInt(editText.getText().toString()));

            //Újrarajzoljuk az élet
            edge2.Draw(context, graphView);
            MainActivity.graph.putEdgeValue(edge2.getStart(),edge2.getEnd(),edge2.getValue());

            //Ha volt már futtatva Dijkstra, akkor újrafuttajuk a legutóbbi kezdőcsúcsra
            if(Dijkstra.getRanDijkstra()){
                Dijkstra.Run(MainActivity.graph,Dijkstra.getLastStarter());
            }

            //Kilépünk a dialógusból
            dialog.dismiss();
        });

        //Törlés gomb megnyomásának eseménye
        deleteBtn.setOnClickListener((View view) -> {
            //Új dialógust nyitunk a törlés ellenőrzéséhez
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.sureToDeleteEdge)
                    .setTitle(R.string.deleteEdge)
                    .setPositiveButton(R.string.delete, (alertDialog, which) -> {
                        //Pozitív válasz esetén töröljük az élet
                        edge.Delete(graphView);
                    })
                    .setNegativeButton(R.string.cancel, (alertDialog, which) -> {
                        //Negatív válasz esetén visszalépünk
                        alertDialog.cancel();
                    });

            //Létrehozzuk a dialógust és feldobjuk
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            //Kiléünk a dialógusból
            dialog.dismiss();
        });

        //Feldobjuk a dialógust
        dialog.show();
    }

    //Egy csúcs szerkesztésének dialógusa
    public static void EditNode(Context context, Node node){
        //Létrehozzuk a dialógust
        final Dialog dialog = new Dialog(context);

        //Dialógus típusának és kinézetének beállítása
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_dialog);

        //UI elemek
        TextView title = dialog.findViewById(R.id.dialog_title);
        final EditText nameET = dialog.findViewById(R.id.editText);
        Button submitBtn = dialog.findViewById(R.id.generate_btn);
        Button deleteBtn = dialog.findViewById(R.id.delete_button);

        //Cím beállítása aszerint, hogy melyik csúcsot szerkesztjük
        String titleText = context.getString(R.string.node);
        title.setText(titleText);

        //Szövegmezőbe beírjuk a csúcs nevét
        nameET.setText(node.getName());

        //Figyeljük, hogy változik-e a szövegmező
        nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Ha változik, akkor ellenőrizzük, hogy van-e beírva éppen szöveg
                //Eszerint tiltjuk le a szerkesztő gombot vagy sem
                submitBtn.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Új csúcsot hozunk létre (lambda kifejezés miatt kell egy új final típus!)
        final Node node2 = node;


        //A szerkesztő gomb megnyomásának eseménye
        submitBtn.setOnClickListener((View view) -> {
            //Beállítjuk a csúcs nevét a szövegmezőből
            node2.setName(nameET.getText().toString());

            //Újrarajzoljuk a csúcsot
            node2.Draw(context, graphView);

            //Kilépünk a dialógusból
            dialog.dismiss();
        });

        //A törlés gomb megnyomásának eseménye
        deleteBtn.setOnClickListener((View view) -> {
            //Új dialógust nyitunk a törlés ellenőrzéséhez
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.sureToDeleteNode)
                    .setTitle(R.string.deleteNode)
                    .setPositiveButton(R.string.delete, (alertDialog, which) -> {
                        //Pozitív válasz esetén töröljük az élet
                        node.Delete(graphView);

                    })
                    .setNegativeButton(R.string.cancel, (alertDialog, which) -> {
                        //Negatív válasz esetén visszalépünk
                        alertDialog.cancel();
                    });


            //Létrehozzuk a dialógust és feldobjuk
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            //Kilépünk a dialógusból
            dialog.dismiss();
        });

        //Feldobjuk a dialógust
        dialog.show();
    }

    //Dijkstra algoritmus dialógusa
    public static void Dijkstra(Context context){
        //Létrehozzuk a dialógust
        final Dialog dialog = new Dialog(context);

        //Dialógus típusának és kinézetének beállítása
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dijkstra_dialog);

        //UI elemek
        TextView title = dialog.findViewById(R.id.dialog_title);
        Spinner spinner = dialog.findViewById(R.id.node_spinner);
        Button runBtn = dialog.findViewById(R.id.generate_btn);

        //Lemásuljuk egy listába a csúcsokat
        ArrayList<String> nodes = new ArrayList<>();
        for (Node n :
                MainActivity.graph.nodes()) {
            nodes.add(n.getName());
        }

        //Beállítjuk a címet
        title.setText(R.string.select_starting_node);

        //Feltöltünk egy adaptert a csúcsok listájával, majd hozzárendeljük a spinner-hez
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,nodes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Futtatás gomb megnyomásának eseménye
        runBtn.setOnClickListener((View view) -> {
            //Lemásoljuk a gráfot
            MutableValueGraph<Node,Integer> graph = MainActivity.graph;

            //Kijelöljük a kezdőcsúcsot a spinner-ben való kiválasztással
            Node startNode = null;
            for (Node n :
                    graph.nodes()) {
                if (n.getName() == spinner.getSelectedItem()){
                    startNode = n;
                    startNode.setType(Node.NodeType.START);
                }
                //A többi elem alap típusú lesz
                else
                    n.setType(Node.NodeType.BASIC);

                //Újrarajzoljuk a csúcsokat
                n.Draw(context, graphView);
            }

            //Ha van csúcs a gráfban
            if(graph.nodes().size()>0){
                //Lefuttatjuk az algoritmust a kijelölt kezdőcsúccsal
                Dijkstra.Run(graph, startNode);
                //Minden élet újrarajzolunk
                for (Edge e : lineView.edges){
                    e.setRoute(false);
                    e.Draw(context, graphView);
                }
                //Frissítjük az élek nézetét
                lineView.update();
            } else {
                //Ha nem volt csúcs a gráfban, akkor feldobunk egy figyelmeztető üzenetet
                Toast.makeText(graphView.getContext(), context.getString(R.string.firstCreateGraph), Toast.LENGTH_SHORT).show();
            }

            //Kilépünk a dialógusból
            dialog.dismiss();
        });

        //Feldobjuk a dialógust
        dialog.show();
    }

    //A gráf generálásának a dialógusa
    public static void Generate(Context context){
        //Létrehozzuk a dialógust
        final Dialog dialog = new Dialog(context);

        //Dialógus típusának és kinézetének beállítása
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.generate_dialog);

        //UI elemek
        TextView title = dialog.findViewById(R.id.dialog_title);
        NumberPicker nodePicker = dialog.findViewById(R.id.node_count_picker);
        NumberPicker edgePicker = dialog.findViewById(R.id.edge_count_picker);
        Button generateBtn = dialog.findViewById(R.id.generate_btn);

        //Beállítjuk a címet
        title.setText(context.getString(R.string.generateGraph));

        //Beállítjuk a minimális és a maximális csúcsok számát a számválasztón
        nodePicker.setMinValue(2);
        nodePicker.setMaxValue(10);

        //A csúcsok számát kivesszük a számválasztóból
        int nodes = nodePicker.getValue();

        //Beállítjuk a minimális és a maximális élek számát a számválasztón
        edgePicker.setMinValue(nodes-1);
        edgePicker.setMaxValue(nodes*(nodes-1)/2);

        //Figyeljük a csúcsok számválasztóját
        nodePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            //Az új érték szerint állítjuk be a minimális és maximális
            //értékét az élek számválasztójának
            edgePicker.setMinValue(newVal-1);
            edgePicker.setMaxValue(newVal*(newVal-1)/2);
        });

        //A generáló gomb megnyomásának eseménye
        generateBtn.setOnClickListener((View view) -> {
            //Legenerájuk a gráfot a korábbi paraméterekkel
            MainActivity.GenerateGraph(context,
                    lineView,
                    graphView,
                    MainActivity.graph,
                    nodePicker.getValue(),
                    edgePicker.getValue()
                    );

            //Kilépünk a dialógusból
            dialog.dismiss();
        });

        //Feldobjuk a dialógust
        dialog.show();
    }

    //Él hozzáadásának a dialógusa
    public static void AddEdge(Context context){
        //Létrehozzuk a dialógust
        final Dialog dialog = new Dialog(context);

        //Dialógus típusának és kinézetének beállítása
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_edge_dialog);

        //UI elemek
        Spinner firstNodeSpinner = dialog.findViewById(R.id.firstNodeSpinner);
        Spinner secondNodeSpinner = dialog.findViewById(R.id.secondNodeSpinner);
        EditText valueET = dialog.findViewById(R.id.valueEditText);
        Button submitBtn = dialog.findViewById(R.id.generate_btn);

        //Lemásoljuk a csúcsok listáját
        ArrayList<Node> nodes = new ArrayList<>();
        for (Node n :
                MainActivity.graph.nodes()) {
            nodes.add(n);
        }

        //Feltöltünk egy adaptert a csúcsok listájával, majd hozzárendeljük az első spinner-hez
        ArrayAdapter<Node> adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,nodes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstNodeSpinner.setAdapter(adapter);

        //Feltöltünk egy adaptert a csúcsok listájával, majd hozzárendeljük a második spinner-hez
        ArrayAdapter<Node> adapter2 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,nodes);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondNodeSpinner.setAdapter(adapter2);

        //Kikapcsoljuk a hozzáadás gombját
        submitBtn.setEnabled(false);

        //A hozzáadás gomb megnyomásának eseménye
        submitBtn.setOnClickListener((View view) -> {
            //A UI elemekből kivesszük a paraméternek szánt adatokat (csúcsok, érték)
            Node start = (Node) firstNodeSpinner.getSelectedItem();
            Node end = (Node) secondNodeSpinner.getSelectedItem();
            int value = Integer.parseInt(valueET.getText().toString());

            //Új élet hozunk létre
            Edge edge = new Edge(start, end, value);

            //Ha volt már ilyen él, akkor figyelmeztető üzenetet dobunk és visszalépünk
            if(graph.hasEdgeConnecting(edge.getStart(),edge.getEnd())){
                Info.ShowToast(context,context.getString(R.string.edgeAlready),Toast.LENGTH_SHORT);
                return;
            }

            //Ha horukélet vennénk fel, akkor figyelmeztető üzenetet dobunk és visszalépünk
            if(start == end){
                Info.ShowToast(context, context.getString(R.string.loopForbidden), Toast.LENGTH_SHORT);
                return;
            }

            //Hozzáadjuk az élet a gráfhoz
            edge.Add();

            //Kilépünk a dialógusból
            dialog.dismiss();
        });

        //Az élköltség szövegmezőjének a változását figyeljük
        valueET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Ha üres a mező, vagy nem szám van benne, akkor kikapcsoljuk a hozzáadás gombot
                submitBtn.setEnabled(s.length() > 0 && Edge.isInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Feldobjuk a dialógust
        dialog.show();
    }

    //A Dijkstra algoritmus eredménye minden csúcsra, dialógusként
    public static void Results(Context context){
        //Ha még nem futott le az algoritmus, akkor figyelmeztető üzenetet dobunk és visszalépünk
        if(!Dijkstra.getRanDijkstra()){
            Info.ShowToast(context,context.getString(R.string.runFirst),Toast.LENGTH_SHORT);
            return;
        }

        //Egy üres string-et felveszünk, ezt fogjuk bővíteni
        String result = "";

        //Minden csúcs elérhetőségét és összköltségét felvesszük egy-egy sorba és hozzáadjuk a string-hez
        for (Node n :
                graph.nodes()) {
            result += "\n" + n.toStringWithPrev() + " (" + n.getSumDistString() + ")";
        }

        //Kivesszük az első sortörést
        result = result.replaceFirst("\n","");

        //Létrehozzuk a dialógust
        final Dialog dialog = new Dialog(context);

        //Dialógus típusának és kinézetének beállítása
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.results_dialog);

        //UI elemek
        TextView resultTV = dialog.findViewById(R.id.results);
        Button submitBtn = dialog.findViewById(R.id.generate_btn);

        //Beállítjuk a string-et
        resultTV.setText(result);

        //Gombnyomásra kilépünk a dialógusból
        submitBtn.setOnClickListener((View view) -> dialog.dismiss());

        //Feldobjuk a dialógust
        dialog.show();
    }
}
