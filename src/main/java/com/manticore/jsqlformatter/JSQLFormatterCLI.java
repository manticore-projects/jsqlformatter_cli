/**
 * Manticore Projects JSQLFormatter is a SQL Beautifying and Formatting Software.
 * Copyright (C) 2023 Andreas Reichel <andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manticore.jsqlformatter;

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.listing.ListingTreePrinter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import javax.swing.tree.TreeNode;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * A powerful Java SQL Formatter based on the JSQLParser.
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JSQLFormatterCLI {
  private static final Logger LOGGER = Logger.getLogger(JSQLFormatterCLI.class.getName());

  public static File getAbsoluteFile(String filename) {
    String homePath = new File(System.getProperty("user.home")).toURI().getPath();

    String _filename = filename.replaceFirst("~", Matcher.quoteReplacement(homePath))
        .replaceFirst("\\$\\{user.home}", Matcher.quoteReplacement(homePath));

    File f = new File(_filename);
    if (!f.isAbsolute()) {
      Path basePath = Paths.get("").toAbsolutePath();

      Path resolvedPath = basePath.resolve(filename);
      Path absolutePath = resolvedPath.normalize();
      f = absolutePath.toFile();
    }
    return f;
  }

  public static String getAbsoluteFileName(String filename) {
    return getAbsoluteFile(filename).getAbsolutePath();
  }

  public static void addFormatterOption(CommandLine line, ArrayList<String> formatterOptions) {
    for (JSQLFormatter.FormattingOption option : JSQLFormatter.FormattingOption.values()) {
      if (line.hasOption(option.optionName)) {
        formatterOptions.add(option.optionName + "=" + line.getOptionValue(option.optionName));
      }
    }
  }

  /**
   * @param args The Command Line Parameters.
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public static void main(String[] args) throws Exception {
    Options options = new Options();

    options.addOption("i", "inputFile", true, "The input SQL file or folder.");
    options.addOption("o", "outputFile", true, "The out SQL file for the formatted statements.");

    OptionGroup formatOptions = new OptionGroup();
    formatOptions.addOption(
        Option.builder("f").longOpt(JSQLFormatter.FormattingOption.OUTPUT_FORMAT.toString())
            .hasArg().desc("The output-format.\n[PLAIN* ANSI HTML RTF]").build());
    formatOptions.addOption(
        Option.builder(null).longOpt("ansi").desc("Output ANSI annotated text.").build());
    formatOptions.addOption(
        Option.builder(null).longOpt("html").desc("Output HTML annotated text.").build());
    options.addOptionGroup(formatOptions);

    OptionGroup indentOptions = new OptionGroup();
    indentOptions.addOption(
        Option.builder("t").longOpt(JSQLFormatter.FormattingOption.INDENT_WIDTH.toString()).hasArg()
            .desc("The indent width.\n[2 4* 8]").build());
    indentOptions.addOption(Option.builder("2").desc("Indent with 2 characters.").build());
    indentOptions.addOption(Option.builder("8").desc("Indent with 8 characters.").build());
    options.addOptionGroup(indentOptions);

    options.addOption(
        Option.builder(null).longOpt(JSQLFormatter.FormattingOption.KEYWORD_SPELLING.toString())
            .hasArg().desc("Keyword spelling.\n[UPPER*, LOWER, CAMEL, KEEP]").build());

    options.addOption(
        Option.builder(null).longOpt(JSQLFormatter.FormattingOption.FUNCTION_SPELLING.toString())
            .hasArg().desc("Function name spelling.\n[UPPER, LOWER, CAMEL*, KEEP]").build());

    options.addOption(
        Option.builder(null).longOpt(JSQLFormatter.FormattingOption.OBJECT_SPELLING.toString())
            .hasArg().desc("Object name spelling.\n[UPPER, LOWER*, CAMEL, KEEP]").build());

    options.addOption(
        Option.builder(null).longOpt(JSQLFormatter.FormattingOption.SEPARATION.toString()).hasArg()
            .desc("Position of the field separator.\n[BEFORE*, AFTER]").build());

    options.addOption(Option.builder(null)
        .longOpt(JSQLFormatter.FormattingOption.SQUARE_BRACKET_QUOTATION.toString()).hasArg()
        .desc("Interpret Square Brackets as Quotes instead of Arrays.\n[AUTO*, YES, NO]").build());

    options.addOption(
        Option.builder(null).longOpt(JSQLFormatter.FormattingOption.SHOW_LINE_NUMBERS.toString())
            .hasArg().desc("Show Line Numbers.\n[YES, NO*]").build());

    options.addOption(
        Option.builder(null).longOpt(JSQLFormatter.FormattingOption.BACKSLASH_QUOTING.toString())
            .hasArg().desc("Allow Back Slash '\\' for escaping.\n[YES, NO*]").build());

    // create the parser
    CommandLineParser parser = new DefaultParser();
    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      ArrayList<String> formatterOptions = new ArrayList<>();

      if (line.hasOption("ansi")) {
        JSQLFormatter.FormattingOption.OUTPUT_FORMAT
            .addFormatterOption(JSQLFormatter.OutputFormat.ANSI.toString(), formatterOptions);
      }

      if (line.hasOption("html")) {
        JSQLFormatter.FormattingOption.OUTPUT_FORMAT
            .addFormatterOption(JSQLFormatter.OutputFormat.HTML.toString(), formatterOptions);
      }

      if (line.hasOption("2")) {
        JSQLFormatter.FormattingOption.INDENT_WIDTH.addFormatterOption("2", formatterOptions);
      }

      if (line.hasOption("8")) {
        JSQLFormatter.FormattingOption.INDENT_WIDTH.addFormatterOption("4", formatterOptions);
      }

      addFormatterOption(line, formatterOptions);

      if (line.hasOption("help") || line.getOptions().length == 0 && line.getArgs().length == 0) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);

        String startupCommand =
            System.getProperty("java.vm.name").equalsIgnoreCase("Substrate VM") ? "./JSQLFormatter"
                : "java -jar JSQLFormatter.jar";

        formatter.printHelp(startupCommand, options, true);
        return;
      }

      File inputFile = null;
      if (line.hasOption("inputFile")) {
        inputFile = getAbsoluteFile(line.getOptionValue("inputFile"));

        if (!inputFile.canRead()) {
          throw new Exception(
              "Can't read the specified INPUT-FILE " + inputFile.getCanonicalPath());
        }

        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
          String sqlStr = IOUtils.toString(inputStream, Charset.defaultCharset());
          System.out.println("\n-- FROM " + inputFile.getName() + "\n" + JSQLFormatter
              .format(sqlStr, formatterOptions.toArray(new String[formatterOptions.size()])));
        } catch (Exception ex) {
          throw new Exception("Error when reading from INPUT FILE " + inputFile.getAbsolutePath(),
              ex);
        }
      }

      List<String> argsList = line.getArgList();
      if (argsList.isEmpty() && !line.hasOption("input-file")) {
        throw new Exception("No SQL statements provided for formatting.");
      } else {
        for (String s : argsList) {
          try {
            System.out.println("\n-- FROM ARGUMENT LIST\n" + JSQLFormatter.format(s,
                formatterOptions.toArray(new String[formatterOptions.size()])));
          } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to format statement\n" + s, ex);
          }
        }
      }

    } catch (ParseException ex) {
      LOGGER.log(Level.FINE, "Parsing failed.  Reason: " + ex.getMessage(), ex);

      HelpFormatter formatter = new HelpFormatter();
      formatter.setOptionComparator(null);
      formatter.printHelp("java -jar H2MigrationTool.jar", options, true);

      throw new Exception("Could not parse the Command Line Arguments.", ex);
    }
  }

  /**
   * Format a list of SQL Statements.
   *
   * <p>
   * SELECT, INSERT, UPDATE and MERGE statements are supported.
   *
   * @param thread The
   * @param sql The SQL Statements to beautify.
   * @param options The Formatting Options (List of "key = value" pairs).
   * @return The beautifully formatted SQL Statements, semi-colon separated.
   */
  @CEntryPoint(name = "format")
  public static CCharPointer format(IsolateThread thread, CCharPointer sql, CCharPointer options) {
    String sqlStr = CTypeConversion.toJavaString(sql);

    String[] optionStr = CTypeConversion.toJavaString(options).split(",");
    try {
      sqlStr = JSQLFormatter.format(sqlStr, optionStr);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    try (CTypeConversion.CCharPointerHolder holder = CTypeConversion.toCString(sqlStr)) {
      final CCharPointer result = holder.get();
      return result;
    }
  }


  public static StringBuilder formatToJava(String sqlStr, int indent, String... options)
      throws Exception {
    String formatted = JSQLFormatter.format(sqlStr, options);
    StringReader stringReader = new StringReader(formatted);
    BufferedReader bufferedReader = new BufferedReader(stringReader);
    String line;
    StringBuilder builder = new StringBuilder();
    int i = 0;
    while ((line = bufferedReader.readLine()) != null) {
      if (i > 0) {
        for (int j = 0; j < indent - 2; j++) {
          builder.append(" ");
        }
        builder.append("+ ");
      } else {
        for (int j = 0; j < indent; j++) {
          builder.append(" ");
        }
      }
      builder.append("\"").append(line).append("\"\n");
      i++;
    }
    return builder;
  }

  public static ArrayList<JavaObjectNode> getAstNodes(String sqlStr, String... options)
      throws Exception {
    ArrayList<JavaObjectNode> nodes = new ArrayList<>();

    Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
    for (Statement statement : statements) {
      JavaObjectNode node = new JavaObjectNode(null, "Statements", statement);
      nodes.add(node);
    }
    return nodes;
  }

  public static SimpleTreeNode translateNode(TreeNode node) {
    SimpleTreeNode simpleTreeNode = new SimpleTreeNode(node.toString());
    Enumeration<? extends TreeNode> children = node.children();
    while (children.hasMoreElements()) {
      simpleTreeNode.addChild(translateNode(children.nextElement()));
    }

    return simpleTreeNode;
  }

  public static String encodeObject(Object object) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
    objectOutput.writeObject(object);
    objectOutput.flush();
    objectOutput.close();
    byteArrayOutputStream.flush();

    return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
  }

  public static String formatToTree(String sqlStr, String... options) throws Exception {
    JSQLFormatter.applyFormattingOptions(options);

    // The Java TreeNode Structure
    JSQLFormatterCLI.JavaObjectNode[] nodes =
        JSQLFormatterCLI.getAstNodes(sqlStr).toArray(new JSQLFormatterCLI.JavaObjectNode[0]);

    SimpleTreeNode rootNode = new SimpleTreeNode("SQL Text");
    for (JSQLFormatterCLI.JavaObjectNode node : nodes) {
      rootNode.addChild(translateNode(node));
    }

    return new ListingTreePrinter().stringify(rootNode);
  }

  private static StringBuilder appendToXML(StringBuilder builder, JavaObjectNode node, int indent)
      throws IOException {

    if (node.isLeaf()) {
      builder.append(StringUtils.leftPad("", indent * 4)).append("<")
          .append(node.object.getClass().getSimpleName()).append(" type='")
          .append(node.object.getClass().getSimpleName()).append("'").append(" class='")
          .append(node.object.getClass().getName()).append("'").append(" object='")
          .append(encodeObject(node.object)).append("'").append(">").append(node.object)
          .append("</").append(node.object.getClass().getSimpleName()).append(">\n");
      // } else if (node.object instanceof net.sf.jsqlparser.schema.Column
      // || node.object instanceof net.sf.jsqlparser.schema.Table
      // || node.object instanceof net.sf.jsqlparser.schema.Database
      // || node.object instanceof net.sf.jsqlparser.schema.Sequence
      // || node.object instanceof net.sf.jsqlparser.schema.Server
      // || node.object instanceof net.sf.jsqlparser.schema.Synonym) {
      // return formatClassName(object);
      // } else if (node.object instanceof Collection) {
      // return formatCollection((Collection) object);
    } else {
      builder.append(StringUtils.leftPad("", indent * 4)).append("<").append(node.fieldName)
          .append(" type='").append(node.object.getClass().getSimpleName()).append("'")
          .append(" class='").append(node.object.getClass().getName()).append("'")
          .append(" object='").append(encodeObject(node.object)).append("'").append(">\n");

      Enumeration<? extends TreeNode> children = node.children();
      while (children.hasMoreElements()) {
        appendToXML(builder, (JavaObjectNode) children.nextElement(), indent + 1);
      }

      builder.append(StringUtils.leftPad("", indent * 4)).append("</").append(node.fieldName)
          .append(">\n");

    }
    return builder;
  }

  public static String formatToXML(String sqlStr, String... options) throws Exception {
    JSQLFormatter.applyFormattingOptions(options);

    StringBuilder builder = new StringBuilder();
    JSQLFormatterCLI.JavaObjectNode[] nodes =
        JSQLFormatterCLI.getAstNodes(sqlStr).toArray(new JSQLFormatterCLI.JavaObjectNode[0]);

    for (JSQLFormatterCLI.JavaObjectNode node : nodes) {
      appendToXML(builder, node, 0);
    }

    return builder.toString();
  }

  public static <T> Collection<T> extract(String sql, Class<T> clazz, String xpath)
      throws Exception {
    ArrayList<T> objects = new ArrayList<>();

    String xmlStr = formatToXML(sql);
    Document doc = Jsoup.parse(xmlStr, "", Parser.xmlParser());
    Elements elements = doc.selectXpath(xpath);
    for (Element element : elements) {
      String className = element.attr("class");
      String attrStr = element.attr("object");

      if (clazz.getName().equals(className)) {
        byte[] bytes = Base64.getDecoder().decode(attrStr);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object o = objectInputStream.readObject();
        objectInputStream.close();

        try {
          objects.add((T) o);
        } catch (Exception ex) {
          // @ todo: this should be ignored as we test for equal class names already
          LOGGER.log(Level.WARNING,
              "Failed to translate a " + o.getClass().getName() + " into a " + clazz.getName());
        }
      }
    }
    return objects;
  }

  public static class JavaObjectNode implements TreeNode {
    private final TreeNode parent;
    private final ArrayList<TreeNode> children = new ArrayList<>();
    public String fieldName;
    public Object object;

    public JavaObjectNode(TreeNode parent, String fieldName, Object object) {
      this.parent = parent;
      this.fieldName = fieldName;
      this.object = object;
      addChildren();
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private void addChildren() {
      ArrayList<Field> fields = new ArrayList<>(FieldUtils.getAllFieldsList(object.getClass()));

      for (Field field : fields) {
        try {
          // System.out.println(object.getClass().getName() + " : " + field);
          Object child = FieldUtils.readField(field, this.object, true);
          if (!(object instanceof Column)) {
            if (child.getClass().getName().startsWith("net.sf.jsqlparser")
                && !child.getClass().getName().startsWith("net.sf.jsqlparser.parser")
                && !child.getClass().isEnum()) {
              JavaObjectNode childNode = new JavaObjectNode(this, field.getName(), child);
              children.add(childNode);
            } else if (child instanceof Collection) {
              Collection<?> collection = (Collection<?>) child;
              if (!collection.isEmpty()
                  && collection.toArray()[0].getClass().getName().startsWith("net.sf.jsqlparser")) {
                for (Object element : collection) {
                  if (element.getClass().getName().startsWith("net.sf.jsqlparser")) {
                    JavaObjectNode subChildNode =
                        new JavaObjectNode(this, field.getName(), element);
                    this.children.add(subChildNode);
                  }
                }
              }
            }
          }
        } catch (Exception ex) {
          LOGGER.log(Level.FINE, "failed to process field " + field.getName(), ex);
        }
      }
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
      return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
      return children.size();
    }

    @Override
    public TreeNode getParent() {
      return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
      return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
      return true;
    }

    @Override
    public boolean isLeaf() {
      return children.isEmpty();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
      return Collections.enumeration(children);
    }


    private String formatClassName(Object o) {
      if (JSQLFormatter.getOutputFormat().equals(JSQLFormatter.OutputFormat.HTML)) {
        return "<html><font color='gray'>" + o.getClass().getSimpleName() + ":</font> <em>" + o
            + "</em></html>";
      } else if (JSQLFormatter.getOutputFormat().equals(JSQLFormatter.OutputFormat.ANSI)) {
        return JSQLFormatter.ANSI_FORMAT_KEYWORD.format(o.getClass().getSimpleName()) + ": "
            + JSQLFormatter.ANSI_FORMAT_PARAMETER.format(o.toString());
      } else {
        return o.getClass().getSimpleName() + ": " + o;
      }
    }

    private String formatFieldClassName(Object o) {
      if (JSQLFormatter.getOutputFormat().equals(JSQLFormatter.OutputFormat.HTML)) {
        return "<html><font color='gray'>" + fieldName + ":</font> <em>"
            + o.getClass().getCanonicalName() + "</em></html>";
      } else if (JSQLFormatter.getOutputFormat().equals(JSQLFormatter.OutputFormat.ANSI)) {
        return JSQLFormatter.ANSI_FORMAT_KEYWORD.format(fieldName) + ": "
            + JSQLFormatter.ANSI_FORMAT_PARAMETER
                .format(o.getClass().getCanonicalName().replace("net.sf.jsqlparser.", ""));
      } else {
        return fieldName + ": "
            + object.getClass().getCanonicalName().replace("net.sf.jsqlparser.", "");
      }
    }

    private String formatCollection(Collection<?> collection) {
      if (JSQLFormatter.getOutputFormat().equals(JSQLFormatter.OutputFormat.HTML)) {
        return "<html><font color='gray'>" + fieldName + " -></font> Collection&lt;"
            + collection.toArray()[0].getClass().getSimpleName() + "&gt;</html>";
      } else if (JSQLFormatter.getOutputFormat().equals(JSQLFormatter.OutputFormat.ANSI)) {
        return JSQLFormatter.ANSI_FORMAT_KEYWORD.format(fieldName) + " -> Collection<"
            + JSQLFormatter.ANSI_FORMAT_PARAMETER
                .format(collection.toArray()[0].getClass().getSimpleName())
            + ">";
      } else {
        return object.getClass().getSimpleName() + ": " + object;
      }
    }

    @Override
    public String toString() {
      if (object instanceof Column || object instanceof Table
          || object instanceof net.sf.jsqlparser.schema.Database
          || object instanceof net.sf.jsqlparser.schema.Sequence
          || object instanceof net.sf.jsqlparser.schema.Server
          || object instanceof net.sf.jsqlparser.schema.Synonym) {
        return formatClassName(object);
      } else if (object instanceof Collection) {
        return formatCollection((Collection<?>) object);
      } else if (isLeaf()) {
        return formatClassName(object);
      } else {
        return formatFieldClassName(object);
      }
    }
  }
}
