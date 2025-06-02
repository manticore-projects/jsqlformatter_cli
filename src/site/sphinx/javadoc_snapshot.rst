
#######################################################################
API 5.1-SNAPSHOT
#######################################################################

Base Package: com.manticore.jsqlformatter


..  _com.manticore.jsqlformatter:
***********************************************************************
Base
***********************************************************************

..  _com.manticore.jsqlformatter.FragmentContentHandler:

=======================================================================
FragmentContentHandler
=======================================================================

*extends:* :ref:`DefaultHandler<org.xml.sax.helpers.DefaultHandler>` 

| **FragmentContentHandler** (xmlReader, builder)
|          :ref:`XMLReader<org.xml.sax.XMLReader>` xmlReader
|          :ref:`StringBuilder<java.lang.StringBuilder>` builder



                |          :ref:`String<java.lang.String>` xPath

                |          :ref:`XMLReader<org.xml.sax.XMLReader>` xmlReader

                |          :ref:`FragmentContentHandler<com.manticore.jsqlformatter.FragmentContentHandler>` parent

                |          :ref:`StringBuilder<java.lang.StringBuilder>` builder

            | *@Override*
| **startElement** (uri, localName, qName, atts)
|          :ref:`String<java.lang.String>` uri
|          :ref:`String<java.lang.String>` localName
|          :ref:`String<java.lang.String>` qName
|          :ref:`Attributes<org.xml.sax.Attributes>` atts


| *@Override*
| **endElement** (uri, localName, qName)
|          :ref:`String<java.lang.String>` uri
|          :ref:`String<java.lang.String>` localName
|          :ref:`String<java.lang.String>` qName


| *@Override*
| **characters** (ch, start, length)
|          char[] ch
|          int start
|          int length


| **getXPath** (xml) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` xml
|          returns :ref:`String<java.lang.String>`




..  _com.manticore.jsqlformatter.JSQLFormatterCLI:

=======================================================================
JSQLFormatterCLI
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| A powerful Java SQL Formatter based on the JSQLParser.

| **JSQLFormatterCLI** ()


| **getAbsoluteFile** (filename) → :ref:`File<java.io.File>`
|          :ref:`String<java.lang.String>` filename
|          returns :ref:`File<java.io.File>`



| **getAbsoluteFileName** (filename) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` filename
|          returns :ref:`String<java.lang.String>`



| **addFormatterOption** (line, formatterOptions)
|          :ref:`CommandLine<org.apache.commons.cli.CommandLine>` line
|          :ref:`String><java.util.ArrayList<java.lang.String>>` formatterOptions


| *@SuppressWarnings*
| **main** (args)
|          :ref:`String[]<java.lang.String[]>` args


| *@CEntryPoint*
| **format** (thread, sql, options) → :ref:`CCharPointer<org.graalvm.nativeimage.c.type.CCharPointer>`
| Format a list of SQL Statements. 
| SELECT, INSERT, UPDATE and MERGE statements are supported.
|          :ref:`IsolateThread<org.graalvm.nativeimage.IsolateThread>` thread
|          :ref:`CCharPointer<org.graalvm.nativeimage.c.type.CCharPointer>` sql
|          :ref:`CCharPointer<org.graalvm.nativeimage.c.type.CCharPointer>` options
|          returns :ref:`CCharPointer<org.graalvm.nativeimage.c.type.CCharPointer>`



| **formatToJava** (sqlStr, indent, options) → :ref:`StringBuilder<java.lang.StringBuilder>`
|          :ref:`String<java.lang.String>` sqlStr
|          int indent
|          :ref:`String[]<java.lang.String[]>` options
|          returns :ref:`StringBuilder<java.lang.StringBuilder>`



| **getAstNodes** (sqlStr, options) → :ref:`JavaObjectNode><java.util.ArrayList<com.manticore.jsqlformatter.JSQLFormatterCLI.JavaObjectNode>>`
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`String[]<java.lang.String[]>` options
|          returns :ref:`JavaObjectNode><java.util.ArrayList<com.manticore.jsqlformatter.JSQLFormatterCLI.JavaObjectNode>>`



| **translateNode** (node) → :ref:`SimpleTreeNode<hu.webarticum.treeprinter.SimpleTreeNode>`
|          :ref:`TreeNode<javax.swing.tree.TreeNode>` node
|          returns :ref:`SimpleTreeNode<hu.webarticum.treeprinter.SimpleTreeNode>`



| **encodeObject** (object) → :ref:`String<java.lang.String>`
|          :ref:`Object<java.lang.Object>` object
|          returns :ref:`String<java.lang.String>`



| **formatToTree** (sqlStr, options) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`String[]<java.lang.String[]>` options
|          returns :ref:`String<java.lang.String>`




                |          :ref:`StringBuilder<java.lang.StringBuilder>` builder

                |          :ref:`JavaObjectNode<com.manticore.jsqlformatter.JSQLFormatterCLI.JavaObjectNode>` node

                |          int indent

                |          returns :ref:`StringBuilder<java.lang.StringBuilder>`


                
            | **formatToXML** (sqlStr, options) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sqlStr
|          :ref:`String[]<java.lang.String[]>` options
|          returns :ref:`String<java.lang.String>`



| **extract** (sql, clazz, xpath) → :ref:`Collection<T><java.util.Collection<T>>`
|          :ref:`String<java.lang.String>` sql
|          :ref:`Class<T><java.lang.Class<T>>` clazz
|          :ref:`String<java.lang.String>` xpath
|          returns :ref:`Collection<T><java.util.Collection<T>>`




..  _com.manticore.jsqlformatter.JSQLFormatterCLI.JavaObjectNode:

=======================================================================
JavaObjectNode
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`TreeNode<javax.swing.tree.TreeNode>` 

| **JavaObjectNode** (parent, fieldName, object)
|          :ref:`TreeNode<javax.swing.tree.TreeNode>` parent
|          :ref:`String<java.lang.String>` fieldName
|          :ref:`Object<java.lang.Object>` object



                |          returns void


                
                    
                        
                        PMD.CyclomaticComplexity
                    
                
            | *@Override*
| **getChildAt** (childIndex) → :ref:`TreeNode<javax.swing.tree.TreeNode>`
|          int childIndex
|          returns :ref:`TreeNode<javax.swing.tree.TreeNode>`



| *@Override*
| **getChildCount** () → int
|          returns int



| *@Override*
| **getParent** () → :ref:`TreeNode<javax.swing.tree.TreeNode>`
|          returns :ref:`TreeNode<javax.swing.tree.TreeNode>`



| *@Override*
| **getIndex** (node) → int
|          :ref:`TreeNode<javax.swing.tree.TreeNode>` node
|          returns int



| *@Override*
| **getAllowsChildren** () → boolean
|          returns boolean



| *@Override*
| **isLeaf** () → boolean
|          returns boolean



| *@Override*
| **children** () → :ref:`TreeNode><java.util.Enumeration<? extends javax.swing.tree.TreeNode>>`
|          returns :ref:`TreeNode><java.util.Enumeration<? extends javax.swing.tree.TreeNode>>`




                |          :ref:`Object<java.lang.Object>` o

                |          returns :ref:`String<java.lang.String>`


            
                |          :ref:`Object<java.lang.Object>` o

                |          returns :ref:`String<java.lang.String>`


            
                |          :ref:`Collection<?><java.util.Collection<?>>` collection

                |          returns :ref:`String<java.lang.String>`


            | *@Override*
| **toString** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`




..  _com.manticore.jsqlformatter.JavaTools:

=======================================================================
JavaTools
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| A powerful Java SQL Formatter based on the JSQLParser.

| **JavaTools** ()


| **main** (args)
|          :ref:`String[]<java.lang.String[]>` args


| **formatJava** (javaCode, options) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` javaCode
|          :ref:`String[]<java.lang.String[]>` options
|          returns :ref:`String<java.lang.String>`



| **toJavaString** (sql) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sql
|          returns :ref:`String<java.lang.String>`



| **toJavaStringBuilder** (sql) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sql
|          returns :ref:`String<java.lang.String>`



| **toJavaMessageFormat** (sql) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` sql
|          returns :ref:`String<java.lang.String>`




                |          :ref:`StringBuilder<java.lang.StringBuilder>` builder

                |          :ref:`ParseTreeNode<org.snt.inmemantlr.tree.ParseTreeNode>` p

                |          int indent

                |          :ref:`LocalVariableDeclaration><java.util.ArrayList<com.manticore.jsqlformatter.JavaTools.LocalVariableDeclaration>>` declarations

                |          returns void


            