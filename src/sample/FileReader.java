package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FileReader {
    int radi = 16;
    int id = 0;
    ArrayList<Integer> deltaDist = new ArrayList<>(30);
    ArrayList<Pair<Integer, String>> rolesChanged;

    void drawFromFile(GraphicsContext gc, int xOff, int yOff, String fileName) {
        deltaDist = new ArrayList<>(30);
        rolesChanged = new ArrayList<>(10);
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(3);
        boolean hasDrawnFirst = false;
        boolean previousWasD2 = false;
        int dToB2 = 0;
        int dToB1 = 0;
        Point prev = null;

        boolean regrouping = false;
        boolean regroupingReceived = false;

        int waypointNr = 0;
        int lastRoleAddTime = 0;
        try {
            FileInputStream fstream = new FileInputStream(fileName);

            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String line;
            while ((line = br.readLine()) != null) {
                // process the line.

                if (line.contains("Id from hostname")) {
                    ArrayList<Integer> ints = getIntsFromString(line);
                    if (ints.isEmpty()) continue;
                    id = ints.get(0);
                    switch (id) {
                        case 15:
                            gc.setStroke(Color.GREEN);
                            break;
                        case 16:
                            gc.setStroke(Color.RED);
                            break;
                        case 17:
                            gc.setStroke(Color.ALICEBLUE);
                            break;
                    }

                }

                if (line.contains("added behaviour:")) {
                    ArrayList<Double> d = getDoublesFromString(line);
                    if (d.isEmpty()) continue;
                    double time = d.get(0);
                    int timeintSec = (int) time;
                    lastRoleAddTime = timeintSec;
                    String[] split = line.split("\\s+");
                    String roleAdded = split[split.length - 1].substring(2);
                    rolesChanged.add(new Pair<>(timeintSec, "+ " + roleAdded));

                }
                if (line.contains("removing behaviour:")) {

                    ArrayList<Double> d = getDoublesFromString(line);
                    if (d.isEmpty()) continue;
                    double time = d.get(0);
                    int timeintSec = (int) time;
                    String[] split = line.split("\\s+");
                    String roleAdded = split[split.length - 1].substring(2);
                    rolesChanged.add(new Pair<>(timeintSec, "- " + roleAdded));

                }

                if (line.contains("new target request")) {
                    //regrouping = true;
                    regroupingReceived=true;
                }
                if (previousWasD2 && line.contains("calcAngle called")) {
                    previousWasD2 = false;

                    ArrayList<Integer> ints = getIntsFromString(line);
                    dToB1 = ints.get(1);
                    System.out.println("dtob1 2");

                }
                if (line.contains("angle1")) {
                    ArrayList<Double> ds = getDoublesFromString(line);
                    if (ds.size() < 2) continue;
                    System.out.println("all 3");

                    Point b1 = getDxDy(Math.toRadians(ds.get(0)), dToB1 / 10);
                    Point b2 = getDxDy(Math.toRadians(ds.get(1)), dToB2 / 10);
                    Point b0 = new Point(0, 0);
                    Paint p = gc.getStroke();
                    gc.setStroke(Color.GRAY);

                    gc.strokeOval(b1.x + xOff - radi, yOff - b1.y - radi, 2 * radi, 2 * radi);
                    gc.strokeOval(b2.x + xOff - radi, yOff - b2.y - radi, 2 * radi, 2 * radi);
                    gc.strokeOval(b0.x + xOff - radi, yOff - b0.y - radi, 2 * radi, 2 * radi);

                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(1);
                    gc.strokeText("B1", b1.x + xOff + radi, yOff - b1.y - radi);
                    gc.strokeText("B2", b2.x + xOff + radi, yOff - b2.y - radi);
                    gc.strokeText("BM", b0.x + xOff + radi, yOff - b0.y - radi);

                    gc.setStroke(p);

                    System.out.println("beacons :");
                    System.out.println(b1.toString());
                    System.out.println(b2.toString());
                    System.out.println(b0.toString());

                }

                if (line.contains("final check measurement")) {
                    ArrayList<Integer> ints = getIntsFromString(line);
                    if (ints.size() < 1) continue;
                    dToB2 = ints.get(0);
                    previousWasD2 = true;
                    System.out.println(line);
                } else if (line.contains("elb distOdo")) {
                    ArrayList<Integer> ints = getIntsFromString(line);
                    if (ints.size() < 2) continue;
                    int dLoc = ints.get(1);
                    int dOdo = ints.get(0) / 10;
                    deltaDist.add(dOdo - dLoc);

                }

                if (line.contains("mttb setting target")) {
                    ArrayList<Integer> ints = getIntsFromString(line);
                    if (ints.size() < 2) continue;
                    int x = ints.get(0);
                    int y = ints.get(1);

                    gc.strokeOval(x + xOff - radi * 2, yOff - y - radi * 2, 2 * radi * 2, 2 * radi * 2);
                    double lw = gc.getLineWidth();
                    gc.setLineWidth(1);
                    gc.strokeText("Mērķis " + id, x + xOff + radi * 2, yOff - y - radi * 2);
                    gc.setLineWidth(lw);
                }


                if (line.contains("localisation done")) {

                    int xind = line.indexOf('x');
                    int yind = line.indexOf('y');
                    if (xind < 0 || yind < xind) continue;
                    String sx = line.substring(xind + 2, yind);
                    String sy = line.substring(yind + 2);
                    sx = sx.replaceAll("\\s+", "");
                    sy = sy.replaceAll("\\s+", "");
                    int x = Integer.parseInt(sx);
                    int y = Integer.parseInt(sy);

                    gc.strokeOval(x + xOff - radi, yOff - y - radi, 2 * radi, 2 * radi);
                    Paint p = gc.getFill();
                    gc.setFill(Color.BLACK);
                    gc.fillText(id + " " + waypointNr++, x + xOff + radi, yOff - y - radi);
                    gc.setFill(p);

                    if (prev != null) {
                        if (regrouping) gc.setLineDashes(5);

                        gc.strokeLine(x + xOff, yOff - y, prev.x + xOff, yOff - prev.y);
                        gc.setLineDashes(0);
                    } else {//mark cur point as start

                        // double lw = gc.getLineWidth();
                        //gc.setLineWidth(1);
                        gc.fillText("Sākums " + id, x + xOff + radi, yOff - y - radi * 2);
                        //gc.setLineWidth(lw);
                        prev = new Point(0, 0);
                    }
                    prev.x = x;
                    prev.y = y;
                    System.out.println("x:: " + sx + " y:: " + sy);
if(regroupingReceived) regrouping=true;
                }
            }


        } catch (IOException e) {
        }
        for (Integer i : deltaDist) {
            System.out.print(i + " ");
        }
        System.out.println();

        ArrayList<Pair<String, Integer>> hist = calcHistogram(75, 6, deltaDist);

        for (Pair<String, Integer> p : hist) {
            System.out.println(p.getKey() + "," + p.getValue());
        }

        for (Pair<Integer, String> p : rolesChanged) {
            System.out.println(p.getKey() + "," + p.getValue());
        }

    }

    ArrayList<Pair<String, Integer>> calcHistogram(int rad, int bins, ArrayList<Integer> data) {
        int binStep = rad * 2 / bins;
        int start = -rad;
        ArrayList<Pair<String, Integer>> res = new ArrayList<>();
        for (int i = -1; i <= bins; i++) {

            int min = start + i * binStep;
            int max = min + binStep;
            String name = "[" + min + ";" + max + ")";
            if (i == -1) {
                min = -Integer.MAX_VALUE;
                max = start;
                name = "[-inf ; " + max + ")";
            } else if (i == bins) {
                min = rad;
                max = Integer.MAX_VALUE;
                name = "[" + min + " ; inf)";
            }


            int count = 0;
            for (Integer in : data) {
                if (in > min && in <= max)
                    count++;
            }
            res.add(new Pair<String, Integer>(name, count));

        }

        return res;
    }

    ArrayList<Integer> getIntsFromString(String s) {
        ArrayList<Integer> r = new ArrayList<>();

        String[] strings = s.split("\\s+");
        if (strings == null) return r;

        for (String st : strings) {
            int i;
            try {
                i = Integer.parseInt(st);
                r.add(i);
            } catch (NumberFormatException e) {
            }

        }

        return r;
    }

    ArrayList<Double> getDoublesFromString(String s) {
        ArrayList<Double> r = new ArrayList<>();

        String[] strings = s.split("\\s+");
        if (strings == null) return r;

        for (String st : strings) {
            double i;
            try {
                i = Double.parseDouble(st);
                r.add(i);
            } catch (NumberFormatException e) {
            }

        }

        return r;
    }

    Point getDxDy(double angleR, int dist) {
        System.out.println("dxdy called " + angleR + " dist: " + dist);
        int dx = (int) (Math.cos(angleR) * dist);
        int dy = (int) (Math.sin(angleR) * dist);

        return new Point(dx, dy);

    }

    static void drawTimeAxisTimeLabels(int startTime,  Canvas c, int nr) {
        GraphicsContext gc = c.getGraphicsContext2D();
        int xoffset = 80;
        int h = 100;
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Ubuntu", FontWeight.EXTRA_LIGHT, FontPosture.REGULAR, 14));

        gc.strokeLine(xoffset, nr * h - 80, c.getWidth(), nr * h- 80);
       // gc.strokeText("Laiks, min " , 5, nr * h - h / 2 + 4);
        gc.fillText("Laiks, min " , 5, nr * h - 80 + 4);

        for (int i = 0; i < c.getWidth(); i++) {
            gc.fillText(i+"" , xoffset+5+i*120, nr * h - 80 -2);

        }

    }

    static int rowH = 15;

    static void drawTimeAxis(int startTime, ArrayList<Pair<Integer, String>> rolesChanged, Canvas c, int nr, int id) {
        GraphicsContext gc = c.getGraphicsContext2D();
        int xoffset = 80;
        int h = 100;
//        BoxBlur blur = new BoxBlur();
//        blur.setWidth(1);
//        blur.setHeight(1);
//        blur.setIterations(2);
//        gc.setEffect(blur);

//       List<String> f = Font.getFamilies();
//
//
//        for (String s:
//             f) {
//            System.out.println("s = " + s);
//        }
       
       
        gc.setFill(Color.BLACK);
        Font font = Font.font("Ubuntu", FontWeight.NORMAL, FontPosture.REGULAR, 14);
        System.out.println("font.toString() = " + font.toString());
        gc.setFont(font);
        gc.setFontSmoothingType(FontSmoothingType.GRAY);

        gc.strokeLine(xoffset, nr * h - h / 2, c.getWidth(), nr * h - h / 2);
       // gc.strokeText("Robots " + id, 5, nr * h - h / 2 + 4);
        gc.fillText("Robots " + id, 5, nr * h - h / 2 + 4);

        TreeMap<Integer, ArrayList<String>> addedRoles = new TreeMap<>();
        TreeMap<Integer, ArrayList<String>> removedRoles = new TreeMap<>();

        for (Pair<Integer, String> p : rolesChanged) {
            addedRoles.putIfAbsent(p.getKey(), new ArrayList<>());
            removedRoles.putIfAbsent(p.getKey(), new ArrayList<>());

            if (p.getValue().contains("+")) {
                addedRoles.get(p.getKey()).add(p.getValue());
            } else {
                removedRoles.get(p.getKey()).add(p.getValue());
            }


        }

        Set<Map.Entry<Integer, ArrayList<String>>> colsAdded = addedRoles.entrySet();
        Set<Map.Entry<Integer, ArrayList<String>>> colsRemoved = removedRoles.entrySet();
        int rowsA = 0;
        int prevX = 0;
        for (Map.Entry<Integer, ArrayList<String>> e : colsAdded) {
            int time = e.getKey();
            ArrayList<String> col = e.getValue();
            int x = time - startTime;
            if (prevX + 90 < x) rowsA = 0;
            prevX = x;
            System.out.println("x = " + x);

            System.out.println("rows = " + rowsA);
            for (String s : col) {
               // gc.strokeText(s, x + xoffset, nr * h - h / 2 - (rowsA * rowH) - 5);
                gc.fillText(s, x*2 + xoffset, nr * h - h / 2 - (rowsA * rowH) - 5);
                rowsA++;
            }

        }

        for (Map.Entry<Integer, ArrayList<String>> e : colsRemoved) {
            int time = e.getKey();
            ArrayList<String> col = e.getValue();
            int x = time - startTime;

            int rows = col.size();

            for (String s : col) {
                gc.fillText(s, x*2 + xoffset, nr * h - h / 2 + (rows * rowH) + rowH / 3);
                rows++;
            }

        }

    }


}
