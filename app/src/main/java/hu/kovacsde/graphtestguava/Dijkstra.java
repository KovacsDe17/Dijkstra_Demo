package hu.kovacsde.graphtestguava;

import com.google.common.graph.MutableValueGraph;

import java.util.HashSet;
import java.util.Set;

public class Dijkstra {

    //Logikai változó: futott-e már az algoritmus
    private static boolean ranDijkstra = false;
    //A legutolsó alkalommal kezdőnek választott csúcs
    private static Node lastStarter = null;

    public static void setRanDijkstra(boolean ran){
        ranDijkstra = ran;
    }

    public static boolean getRanDijkstra(){
        return ranDijkstra;
    }

    public static Node getLastStarter() {
        return lastStarter;
    }

    //Az algoritmus futtatása
    public static void Run(MutableValueGraph<Node, Integer> graph, Node startNode){
        //Rögzítjük legutolsó kezdőként a jelenlegi kezdőcsúcsot
        lastStarter = startNode;

        //Minden csúcsra beállítjuk, hogy "végtelen" költséggel
        //érhető el, és nincsen azt megelőző csúcsa
        for (Node v :
                graph.nodes()) {
            v.setSumDist(Integer.MAX_VALUE);
            v.setPrevoius(null);
        }
        //A kezdőcsúcsot 0 távolságra állítjuk be
        startNode.setSumDist(0);

        //Lemásoljuk a gráfban lévő csúcsok halmazát segédhalmaznak
        Set<Node> copy = new HashSet<>();
        copy.addAll(graph.nodes());

        //Amíg a segédhalmaz ki nem ürül:
        while(!copy.isEmpty()){
            //Kivesszük a legkisebb költséggel elérhető csúcsot
            Node u = ExtractMin(copy);

            //A kivett csúcsnak megnézzük az összes szomszédos csúcsát:
            for (Node v :
                    graph.adjacentNodes(u)) {

                //Alternatív útvonalat adunk meg, amellyel elérhető a kivett csúcs
                int alt = u.getSumDist() + graph.edgeValueOrDefault(u,v,0);

                //Ha kisebb az alternatív út költsége, akkor azt választjuk, majd eszerint
                //frissítjük az adott csúcs elérésének költségét és a megelőző csúcsot
                if(alt < v.getSumDist()){
                    v.setSumDist(alt);
                    v.setPrevoius(u);
                }
            }

        }

        //Jelöljük, hogy már lefutott az algoritmus
        ranDijkstra = true;
    }

    //Kivesszük a halmazból a legkisebb költséggel elérhető csúcsot
    static Node ExtractMin(Set<Node> nodes){
        //A legkisebbnek az első elemet jelöljük meg először
        Node min = (Node) nodes.toArray()[0];

        //Minden csúcsra, ami része a halmaznak:
        for (Node node :
                nodes) {
            //Megnézzük, hogy a vizsgált csúcs elérésének költsége kisebb-e, mint az eddigi minimum
            //és ha igen, akkor az lesz a legkisebb az adott iterációt tekintve
            if(node.getSumDist() < min.getSumDist())
                min = node;
        }

        //A legkisebb elemet eltávolítjuk a halmazból és visszatérünk vele
        nodes.remove(min);
        return min;
    }
}
