package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads a manifest.json written by SequenceControllerThread and restores
 * the sample rows into an NGSTableModel.
 */
public class ManifestLoader {

    public static boolean isManifest(File f) {
        return f.getName().equals("manifest.json");
    }

    /**
     * Parses manifest.json and adds the sample rows to the model.
     * Returns the number of rows loaded, or -1 on parse error.
     */
    public static int load(File manifest, NGSTableModel model) {
        try {
            String json = readAll(manifest);
            List<Map<String, String>> samples = parseSamples(json);
            model.removeAll();
            for (Map<String, String> s : samples) {
                NGS ngs = new NGS();
                model.addNGS(ngs);
                int row = model.getRowCount() - 1;
                setIfPresent(model, row, 0, s.get("r1"));
                setIfPresent(model, row, 2, s.get("reference"));
                setIfPresent(model, row, 3, s.get("alias"));
                setIfPresent(model, row, 4, s.get("left_flank"));
                setIfPresent(model, row, 5, s.get("right_flank"));
                setIfPresent(model, row, 6, s.get("left_primer"));
                setIfPresent(model, row, 7, s.get("right_primer"));
                setIfPresent(model, row, 8, s.get("hdr"));
            }
            return samples.size();
        } catch (IOException e) {
            System.err.println("ManifestLoader: " + e.getMessage());
            return -1;
        }
    }

    private static void setIfPresent(NGSTableModel model, int row, int col, String value) {
        if (value != null && !value.isEmpty()) {
            model.setValueAt(value, row, col);
        }
    }

    private static String readAll(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Minimal JSON parser for the fixed manifest structure.
     * Finds the "samples" array and extracts string fields from each object.
     */
    private static List<Map<String, String>> parseSamples(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        int samplesStart = json.indexOf("\"samples\"");
        if (samplesStart < 0) return result;
        int arrayStart = json.indexOf('[', samplesStart);
        if (arrayStart < 0) return result;

        int depth = 0;
        int objStart = -1;
        for (int i = arrayStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
                if (depth == 1) objStart = i;
            } else if (c == '}') {
                if (depth == 1 && objStart >= 0) {
                    String obj = json.substring(objStart, i + 1);
                    result.add(parseObject(obj));
                    objStart = -1;
                }
                depth--;
                if (depth < 0) break;
            }
        }
        return result;
    }

    private static Map<String, String> parseObject(String obj) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        while (i < obj.length()) {
            int keyStart = obj.indexOf('"', i);
            if (keyStart < 0) break;
            int keyEnd = obj.indexOf('"', keyStart + 1);
            if (keyEnd < 0) break;
            String key = obj.substring(keyStart + 1, keyEnd);
            int colon = obj.indexOf(':', keyEnd + 1);
            if (colon < 0) break;
            // find value start (skip whitespace)
            int valStart = colon + 1;
            while (valStart < obj.length() && Character.isWhitespace(obj.charAt(valStart))) valStart++;
            if (valStart >= obj.length()) break;
            String value;
            if (obj.charAt(valStart) == '"') {
                // string value
                int valEnd = valStart + 1;
                while (valEnd < obj.length()) {
                    if (obj.charAt(valEnd) == '\\') { valEnd += 2; continue; }
                    if (obj.charAt(valEnd) == '"') break;
                    valEnd++;
                }
                value = obj.substring(valStart + 1, valEnd)
                           .replace("\\\\", "\\")
                           .replace("\\\"", "\"");
                i = valEnd + 1;
            } else {
                // non-string value (number, boolean) — read until comma or }
                int valEnd = valStart;
                while (valEnd < obj.length() && obj.charAt(valEnd) != ',' && obj.charAt(valEnd) != '}') valEnd++;
                value = obj.substring(valStart, valEnd).trim();
                i = valEnd;
            }
            map.put(key, value);
        }
        return map;
    }
}
