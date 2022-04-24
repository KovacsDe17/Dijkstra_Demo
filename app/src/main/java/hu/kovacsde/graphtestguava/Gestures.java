package hu.kovacsde.graphtestguava;

import static hu.kovacsde.graphtestguava.MainActivity.lineView;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.DragEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.content.res.ResourcesCompat;

public class Gestures {

    private final static String nodeDragMessage = "Node added";

    //Listener a csúcsok megragadásához
    static View.OnDragListener nodeDragListener = (view, dragEvent) -> {
        //A nézet, amit megfoghatunk (ez lesz a csúcs)
        View draggableItem = (View) dragEvent.getLocalState();

        //Definiáljuk az érintés típusaihoz tartozó akciókat
        switch (dragEvent.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //Ha megragadtuk a csúcsot, akkor érvénytelenítsük a tartalmazó nézetet
                //(jelezzük a nézetnek, hogy valami változott)
                view.invalidate();
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                //Ha elmozdítottuk megragadás közben az ujjunkat az eredeti pozícióból
                //Akkor legyen látható a csúcs, és érvénytelenítsük a tartalmazó nézetet
                draggableItem.setVisibility(View.VISIBLE);
                view.invalidate();
                return true;

            case DragEvent.ACTION_DROP:
                //Ha elengedtük a csúcsot, akkor rögzítjük a felengedés koordinátáit
                float x = dragEvent.getX()-(draggableItem.getWidth()/2);
                float y = dragEvent.getY()-(draggableItem.getHeight()/2);

                //Beállítjuk a csúcs nézetének új helyét
                draggableItem.setX(x);
                draggableItem.setY(y);

                //Beállítjuk a csúcs új helyét
                Node node = Node.FindNodeByView(draggableItem);
                node.getPoint().set((int) dragEvent.getX(),(int) dragEvent.getY());

                //Frissítjük az élekhez tartozó nézetet
                lineView.update();

                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                //Ha lefutott a felengedés művelet, akkor láthatóvá tesszük a csúcsot
                //és érvénytelenítjük a tartalmazó nézetet
                draggableItem.setVisibility(View.VISIBLE);
                view.invalidate();
                return true;

            default: return false;
        }
    };

    //Megragadás listener-jének hozzáadása egy nézethez
    static void AttachViewDragListener(View attachTo){

        //A csúcs lesz a nézet
        RelativeLayout node = (RelativeLayout) attachTo;

        //Hosszú érintésre:
        node.setOnLongClickListener((view)->{
            //Létrehozunk egy új objektumot, ami a mozgatás egy adatát tárolja
            ClipData.Item item = new ClipData.Item(nodeDragMessage);

            //Adattípusok, amiket használni fogunk: egyszerű szöveg
            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

            //Létrehozzuk az adatok tárolásáért felelős objektumot
            ClipData dataToDrag = new ClipData(
                    nodeDragMessage,
                    mimeTypes,
                    item
            );

            //Létrehozzuk a mozgatáshoz az "árnyékot"
            Gestures.NodeDragShadowBuilder nodeShadow = new Gestures.NodeDragShadowBuilder(view);

            //Megkezdhetjük a megragadást
            view.startDrag(dataToDrag,nodeShadow,view,0);

            //Láthatatlanná tesszük a megragadott nézetet
            view.setVisibility(View.INVISIBLE);

            return true;
        });
    }

    //Az "árnyék" létrehozásáért felelős osztály
    static class NodeDragShadowBuilder extends View.DragShadowBuilder {

        private View view;

        public NodeDragShadowBuilder(View view){
            this.view = view;
        }

        //Csatoljuk a megfelelő nézetet
        private Drawable shadow = ResourcesCompat.getDrawable(
                MainActivity.graphView.getResources(),
                R.drawable.node_shadow,
                MainActivity.graphView.getContext().getTheme());

        //Megadjuk, hogy hol helyezkedjen el az "árnyék" az érintéshez képest
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            //Vesszük a szélességét és a magasságát ennek az elemnek
            int width = this.view.getWidth();
            int height = this.view.getHeight();

            //Beállítjuk a méreteket a széllességgel és a magassággal
            shadow.setBounds(0,0,width,height);
            size.set(width,height);

            //Az érintést az elem közepéhez igazítjuk
            touch.set(width/2,height/2);
        }

        //Az "árnyék" kirajzolása
        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }
    }

    //Duplaérintéshez listener
    public static abstract class DoubleClickListener implements View.OnClickListener {
        private int touchCount = 0;

        //Listener az elemek szimpla- és duplakattintásához
        @Override
        public void onClick(View v) {
            long delay = 200;

            touchCount++;

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if(touchCount == 1){
                    OnSingleClick();
                } else if(touchCount == 2){
                    OnDoubleClick();
                }

                touchCount = 0;
            }, delay);
        }

        public abstract void OnSingleClick();
        public abstract void OnDoubleClick();
    }

    //Rezzen a készülék
    public static void Vibrate(long millis){
        Vibrator v = (Vibrator) MainActivity.instance.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(millis);
    }

}
