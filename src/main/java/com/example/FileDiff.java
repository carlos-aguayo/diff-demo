package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;
import print.color.Ansi;
import print.color.ColoredPrinter;

// https://www.diffchecker.com/diff
// https://github.com/dnaumenko/java-diff-utils

public class FileDiff {

  private static List<String> fileToLines(File file) throws IOException {
    final List<String> lines = new ArrayList<String>();
    String line;
    final BufferedReader in = new BufferedReader(new FileReader(file));
    while ((line = in.readLine()) != null) {
      lines.add(line);
    }

    return lines;
  }

  private static final ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
      .foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLUE)
      .build();

  public static void main(String[] args) throws Exception {

    final File original = new File("old1.txt");
    final File revised = new File("new1.txt");

    final List<String> originalFileLines = fileToLines(original);
    final List<String> revisedFileLines = fileToLines(revised);

    DiffRowGenerator drg = new DiffRowGenerator.Builder().
        showInlineDiffs(true).
        ignoreWhiteSpaces(false).
        ignoreBlankLines(false).
        columnWidth(80).
        build();

    List<DiffRow> diffRows = drg.generateDiffRows(originalFileLines, revisedFileLines);
//    for (DiffRow dr : diffRows) {
//      System.out.println("Tag: " + dr.getTag());
//      System.out.println("OldLine: " + dr.getOldLine());
//      System.out.println("NewLine: " + dr.getNewLine());
//    }

    for (DiffRow dr : diffRows) {
      String oldLine = dr.getOldLine();
      switch (dr.getTag()){
      case DELETE:
        cp.println(oldLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.RED);
        break;
      case EQUAL:
        cp.println(oldLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
        break;
      case CHANGE:
        if ("".equals(oldLine)) {
          cp.println(oldLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
        } else {
          cp.println(oldLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.RED);
        }
        break;
      case INSERT:
        cp.println(oldLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.RED);
        default:
      }
    }

    cp.println("========\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);


    for (DiffRow dr : diffRows) {
      String newLine = dr.getNewLine();
      switch (dr.getTag()){
      case DELETE:
        cp.println(newLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
        break;
      case EQUAL:
        cp.println(dr.getOldLine() + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
        break;
      case CHANGE:
        cp.println(newLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.GREEN);
        break;
      case INSERT:
        cp.println(newLine + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.GREEN);
      default:
      }
    }

    boolean execute = false;
    if (execute) {

      final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);
      List<Delta> deltas = patch.getDeltas();
      for (Delta delta : deltas) {
        System.out.println("Type: " + delta.getType());
        Chunk chunk = delta.getRevised();
        System.out.println("Position: " + chunk.getPosition());
        System.out.println("Size: " + chunk.size());
      }

      List<String> left = new ArrayList<>(originalFileLines);
      List<String> right = new ArrayList<>();

      int shift = 0;
      for (Delta delta : deltas) {
        // Changes with a size larger than 1, is a new line in left
        Chunk chunk = delta.getRevised();
        if (delta.getType() == Delta.TYPE.CHANGE) {
          if (chunk.size() > 1) {
            int linesToAdd = chunk.size() - 1;
            int index = chunk.getPosition() + shift;
            for (int i = 0; i < linesToAdd; i++) {
              left.add(index, "");
            }
            shift += linesToAdd;
          }
        }

        // Insert, add line to left
        if (delta.getType() == Delta.TYPE.INSERT) {
          int linesToAdd = chunk.size();
          int index = chunk.getPosition() + shift;
          for (int i = 0; i < linesToAdd; i++) {
            left.add(index, "");
          }
          shift += linesToAdd;
        }

      }
      //  /** A change in the original. */
      //  CHANGE,
      //  /** A delete from the original. */
      //  DELETE,
      //  /** An insert into the original. */
      //  INSERT

      // Changes with a size larger than 1, is a new line in left
      // Delete, add line to right, background gray
      // Insert, add line to left

      for (String s : left) {
        cp.println(s + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.RED);
      }
      cp.println("\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
      for (String s : revisedFileLines) {
        cp.println(s + "\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.RED);
      }

      //    cp.println("Carlos\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.RED);
      //    cp.println("Carlos\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.GREEN);
      //    cp.println("Carlos\n", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.YELLOW);
    }
  }


}
