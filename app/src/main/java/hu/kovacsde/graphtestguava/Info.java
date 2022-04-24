package hu.kovacsde.graphtestguava;

import android.content.Context;
import android.widget.Toast;

public class Info {
    private static Toast MESSAGE;

    //Kiírja az információkat Toast-ban
    public static void ShowToast(Context context, String displayText, int length){
        //Ha van már példánya a Toast-nak, akkor azt bezárjuk
        if(MESSAGE!=null)
            MESSAGE.cancel();

        //Új Toast-ot hozunk létre, amiben az információnk lesz
        MESSAGE = new Toast(context);

        //Az alap szöveg a költséget és az utat mutatja a két csúcs között
        String infoString = displayText;

        //Beállítjuk a szöveget és a megjelenítés hosszát, majd megjelenítjük a Toast-ot
        MESSAGE.setText(infoString);
        MESSAGE.setDuration(length);
        MESSAGE.show();
    }
}
