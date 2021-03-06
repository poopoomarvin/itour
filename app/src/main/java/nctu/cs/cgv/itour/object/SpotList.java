package nctu.cs.cgv.itour.object;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static nctu.cs.cgv.itour.Utility.gpsToImgPx;

/**
 * Created by lobZter on 2017/8/15.
 */

public class SpotList {

    public Map<String, SpotNode> nodeMap;
    public Map<String, SpotNode> fullNodeMap;

    public SpotList(File spotListFile) {
        nodeMap = new LinkedHashMap<>();
        fullNodeMap = new LinkedHashMap<>();
        readSpotsFile(spotListFile);
    }

    private void readSpotsFile(File spotListFile) {

        try {
            FileInputStream inputStream = new FileInputStream(spotListFile);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                String[] arr = line.split(","); // name,lat,lng,order
                if (((int)arr[0].charAt(0)) == 65279) {
                    Log.d("65279" , "gatcha");
                    arr[0] = arr[0].substring(1);
                }
                float[] imgPx = gpsToImgPx(Float.valueOf(arr[1]), Float.valueOf(arr[2]));
                int order = Integer.parseInt(arr[3]);
                if (order < 3) {
                    nodeMap.put(arr[0], new SpotNode(imgPx[0], imgPx[1], arr[1], arr[2], arr[0], order));
                }
                fullNodeMap.put(arr[0], new SpotNode(imgPx[0], imgPx[1], arr[1], arr[2], arr[0], order));


            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getSpotsName() {
        return nodeMap.keySet();
    }

    public Set<String> getFullSpotsName() {
        return fullNodeMap.keySet();
    }
    public Set<String> getPersonalSpotsName() {
        return fullNodeMap.keySet();
    }
}
