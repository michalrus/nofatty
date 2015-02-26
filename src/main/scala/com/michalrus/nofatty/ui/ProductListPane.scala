package com.michalrus.nofatty.ui

import java.awt._
import java.awt.event._
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import javax.swing._
import javax.swing.border.{ EmptyBorder, TitledBorder }
import javax.swing.event.{ DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener }
import javax.swing.table.AbstractTableModel

import com.michalrus.nofatty.Calculator
import com.michalrus.nofatty.data._
import com.michalrus.nofatty.ui.utils._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ProductListPane(onProductsEdited: ⇒ Unit) extends JPanel {

  val filter = new JTextField

  private[this] def setFilter(s: String): Unit = {
    filter.setText(s)
    filter.requestFocus()
    filter.selectAll()
  }

  val addButton = new JButton("+")

  private[this] def errorAlreadyExists(name: String): Unit =
    JOptionPane.showMessageDialog(ProductListPane.this.getRootPane,
      s"Product “$name” already exists.", "Already exists", JOptionPane.ERROR_MESSAGE)

  private[this] val WhiteSpace = """\s+""".r
  private[this] def sanitizeName(name: String): Option[String] = Option(WhiteSpace.replaceAllIn(name, " ")).map(_.trim).filter(_.nonEmpty)

  addButton.addActionListener(new ActionListener {
    val OBasic = "Basic product"
    val OCompound = "Compound product"
    val OCancel = "Cancel"
    override def actionPerformed(e: ActionEvent): Unit = {
      val pane = new JOptionPane("Enter a name for the new product:", JOptionPane.QUESTION_MESSAGE,
        JOptionPane.DEFAULT_OPTION, Unsafe.NullIcon, Array[AnyRef](OBasic, OCompound, OCancel))
      pane.setWantsInput(true)
      val dia = pane.createDialog(ProductListPane.this.getRootPane, "New product")
      dia.setVisible(true)
      dia.dispose()
      val value = Option(pane.getValue).map(_.toString)
      val input = Option(pane.getInputValue).map(_.toString).flatMap(sanitizeName)

      def commit(name: String, p: ⇒ Product): Unit = {
        if (Products.names.get(name).isDefined) errorAlreadyExists(name)
        else Products.commit(p)
        setFilter(name)
      }

      (value, input) match {
        case (Some(OBasic), Some(name))    ⇒ commit(name, BasicProduct(UUID.randomUUID(), DateTime.now, name, NutritionalValue.Zero, "", "", "", "", ""))
        case (Some(OCompound), Some(name)) ⇒ commit(name, CompoundProduct(UUID.randomUUID(), DateTime.now, name, 1.0, "", "", Map.empty))
        case _                             ⇒
      }
    }
  })

  filter.getDocument.addDocumentListener(new DocumentListener {
    import scala.language.reflectiveCalls
    override def insertUpdate(e: DocumentEvent): Unit = productsModel.refresh()
    override def changedUpdate(e: DocumentEvent): Unit = productsModel.refresh()
    override def removeUpdate(e: DocumentEvent): Unit = productsModel.refresh()
  })

  val productsModel = new AbstractListModel[String] {
    def refresh(): Unit = {
      fireContentsChanged(this, 0, Int.MaxValue)
      products.clearSelection()
      products.setSelectedIndex(0)
      onSelectionChanged()
    }
    val predicate: String ⇒ Boolean = _.toLowerCase contains filter.getText.toLowerCase
    override def getSize: Int = Products.names.keySet count predicate
    override def getElementAt(index: Int): String = Products.names.keySet.filter(predicate).toVector.sortWith(_.compareToIgnoreCase(_) < 0).apply(index)
  }

  val product = new AtomicReference[Option[Product]](None)

  val newIngredientsRecord = new AtomicReference[(String, String)](("", ""))

  def onSelectionChanged(): Unit = {
    product.set(Option(products.getSelectedValue) flatMap Products.names.get flatMap Products.find)
    newIngredientsRecord.set(("", ""))
    refresh()
  }

  val products: JList[String] = {
    val l = new JList(productsModel)
    l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    l.setCellRenderer(FilteringListCellRenderer(filter.getText))
    l.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      override def valueChanged(e: ListSelectionEvent): Unit = onSelectionChanged()
    })
    l
  }

  val productRenameAction = new AbstractAction("Rename") {
    override def actionPerformed(e: ActionEvent): Unit = {
      product.get foreach { prod ⇒
        Option(JOptionPane.showInputDialog(ProductListPane.this.getRootPane,
          "Enter a new name:", "Rename", JOptionPane.QUESTION_MESSAGE, Unsafe.NullIcon,
          Unsafe.NullArrayAnyRef, prod.name)).map(_.toString).flatMap(sanitizeName) match {
          case Some(newName) ⇒
            Products.commit(prod match {
              case p: BasicProduct    ⇒ p.copy(name = newName, lastModified = DateTime.now)
              case p: CompoundProduct ⇒ p.copy(name = newName, lastModified = DateTime.now)
            })
            setFilter(newName)
            onProductsEdited
          case _ ⇒
        }
      }
    }
  }

  val productDeleteAction = new AbstractAction("Delete") {
    val DateFormatter = DateTimeFormat.forPattern("yyyy/MM/dd")
    override def actionPerformed(e: ActionEvent): Unit = product.get foreach { prod ⇒
      if (JOptionPane.showConfirmDialog(ProductListPane.this.getRootPane, s"Are you sure you want to delete “${prod.name}”?",
        "Product deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
        Products.delete(prod.uuid) match {
          case Left(Products.DeleteError(uprods, udays)) ⇒
            JOptionPane.showMessageDialog(ProductListPane.this.getRootPane,
              s"Compound products still using this product (first 5 of ${uprods.size}):\n[${uprods take 5 mkString ", "}]\n\n" +
                s"Days still using this product (first 5 of ${udays.size}):\n[${udays take 5 map DateFormatter.print mkString ", "}]",
              "Deletion failed", JOptionPane.WARNING_MESSAGE)
          case _ ⇒
            import language.reflectiveCalls
            productsModel.refresh(); onProductsEdited
        }
    }
  }

  val productPopup = {
    val p = new JPopupMenu

    { val _ = p.add(productRenameAction) }
    { val _ = p.add(productDeleteAction) }
    p
  }

  products.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelected")
  products.getActionMap.put("deleteSelected", productDeleteAction)

  products.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount > 1)
        productRenameAction.actionPerformed(new ActionEvent(e, ActionEvent.ACTION_PERFORMED, "dummy"))
    }
    def showPopup(e: MouseEvent): Unit = {
      val r = products.locationToIndex(e.getPoint)
      if (r >= 0 && r < productsModel.getSize) products.setSelectedIndex(r)
      else products.clearSelection()
      e.getComponent match {
        case l: JList[_] ⇒ productPopup.show(l, e.getX, e.getY)
      }
    }
    override def mousePressed(e: MouseEvent): Unit = if (e.isPopupTrigger) showPopup(e)
    override def mouseReleased(e: MouseEvent): Unit = if (e.isPopupTrigger) showPopup(e)
  })

  val stats = new StatsPane
  stats.setTitle(f"${NutritionalValue.PerGrams}%.0f grams of the product")

  val convertButton = new JButton

  convertButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      product.get foreach { prod ⇒
        val (tpe, counterpart): (String, Product) = prod match {
          case p: BasicProduct    ⇒ ("compound", CompoundProduct(p.uuid, DateTime.now, p.name, 1.0, "", "", Map.empty))
          case p: CompoundProduct ⇒ ("basic", BasicProduct(p.uuid, DateTime.now, p.name, NutritionalValue.Zero, "", "", "", "", ""))
        }
        if (JOptionPane.showConfirmDialog(ProductListPane.this.getRootPane,
          s"Are you sure you want to convert “${prod.name}”\nto a $tpe product? This cannot be undone.",
          "Conversion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
          Products.commit(counterpart)
          product.set(Products.find(counterpart.uuid))
          refresh()
          onProductsEdited
        }
      }
    }
  })

  val ingredientsModel = new AbstractTableModel {
    override def getRowCount = product.get match {
      case Some(prod: CompoundProduct) ⇒ 1 + prod.ingredients.size
      case _                           ⇒ 0
    }
    override def getColumnCount = 2
    override def getColumnName(column: Int) = column match {
      case 0 ⇒ "Ingredient"
      case 1 ⇒ "Grams"
    }
    override def getColumnClass(columnIndex: Int) = classOf[String]
    override def isCellEditable(rowIndex: Int, columnIndex: Int) = true
    override def getValueAt(rowIndex: Int, columnIndex: Int): String =
      if (rowIndex >= getRowCount - 1)
        columnIndex match {
          case 0 ⇒ newIngredientsRecord.get._1
          case _ ⇒ newIngredientsRecord.get._2
        }
      else product.get match {
        case Some(prod: CompoundProduct) ⇒
          val ingrs = prod.ingredients.flatMap { case (uuid, (_, gramsExpr)) ⇒ Products find uuid map ((_, gramsExpr)) }
            .toVector.sortBy(_._1.name)
          columnIndex match {
            case 0 ⇒ ingrs(rowIndex)._1.name
            case _ ⇒ ingrs(rowIndex)._2
          }
        case _ ⇒ ""
      }
    override def setValueAt(aValue: AnyRef, rowIndex: Int, columnIndex: Int): Unit = {
      if (rowIndex == getRowCount - 1) {
        val oldNewRecord = newIngredientsRecord.get
        val newNewRecord: (String, String) = (
          if (columnIndex == 0) aValue.toString else oldNewRecord._1,
          if (columnIndex == 1) aValue.toString else oldNewRecord._2
        )
        newIngredientsRecord.set(newNewRecord)
        if (newNewRecord._1.nonEmpty && newNewRecord._2.nonEmpty)
          product.get match {
            case Some(prod: CompoundProduct) ⇒
              val uuid = Products.names(newNewRecord._1)
              val gramsExpr = newNewRecord._2
              val grams = Calculator(gramsExpr).right.toOption.getOrElse(0.0)
              if (prod.ingredients contains uuid) {
                JOptionPane.showMessageDialog(ProductListPane.this.getRootPane,
                  s"“${newNewRecord._1}” is already a part of “${prod.name}”.", "Adding failed", JOptionPane.WARNING_MESSAGE)
                newIngredientsRecord.set(("", newNewRecord._2))
                fireTableDataChanged()
              }
              else if (!(prod couldContain uuid)) {
                JOptionPane.showMessageDialog(ProductListPane.this.getRootPane,
                  s"Adding “${newNewRecord._1}” to “${prod.name}” would result in a cycle in the products graph.\n" +
                    s"Are you trying to make potatos of french fries? ☺", "Adding failed", JOptionPane.WARNING_MESSAGE)
                newIngredientsRecord.set(("", newNewRecord._2))
                fireTableDataChanged()
              }
              else {
                Products.commit(prod.copy(lastModified = DateTime.now, ingredients = prod.ingredients + (uuid → ((grams, gramsExpr)))))
                onSelectionChanged()
              }
            case _ ⇒
          }
      }
      else {
        // TODO
      }
    }
  }

  val ingredients: BetterTable = {
    val t = new BetterTable(ingredientsModel, (row, _) ⇒ row >= ingredientsModel.getRowCount - 1)
    t.getTableHeader.setReorderingAllowed(false)
    t.getTableHeader.setResizingAllowed(false)

    val colProduct = t.getColumnModel.getColumn(0)
    val colGrams = t.getColumnModel.getColumn(1)

    colProduct.setCellEditor(new AutocompletionCellEditor(Products.names.keySet.toVector.sorted))

    colGrams.setCellEditor(new CalculatorCellEditor(_ > 0.0))
    colGrams.setCellRenderer({
      val r = new CalculatorCellRenderer
      r.setHorizontalAlignment(SwingConstants.RIGHT)
      r
    })

    t
  }

  val massPre, massPost = CalculatorTextfield("", _ > 0.0, allowEmpty = true)

  val nutritionalValues = {
    def g = CalculatorTextfield("", _ >= 0.0, allowEmpty = true)
    Seq[(String, VerifyingTextField, BasicProduct ⇒ String, (BasicProduct, String, Double) ⇒ BasicProduct)](
      ("Energy [kcal]:", g, _.kcalExpr, (p, e, v) ⇒ p.copy(kcalExpr = e, nutrition = p.nutrition.copy(kcal = v))),
      ("Protein [g]:", g, _.proteinExpr, (p, e, v) ⇒ p.copy(proteinExpr = e, nutrition = p.nutrition.copy(protein = v))),
      ("Fat [g]:", g, _.fatExpr, (p, e, v) ⇒ p.copy(fatExpr = e, nutrition = p.nutrition.copy(fat = v))),
      ("Carbohydrate [g]:", g, _.carbohydrateExpr, (p, e, v) ⇒ p.copy(carbohydrateExpr = e, nutrition = p.nutrition.copy(carbohydrate = v))),
      ("Fiber [g]:", g, _.fiberExpr, (p, e, v) ⇒ p.copy(fiberExpr = e, nutrition = p.nutrition.copy(fiber = v)))
    )
  }

  nutritionalValues foreach {
    case (_, field, reader, modifier) ⇒
      field.addFocusListener(new FocusListener {
        override def focusGained(e: FocusEvent): Unit = ()
        override def focusLost(e: FocusEvent): Unit = {
          product.get match {
            case Some(prod: BasicProduct) if field.originalInput != reader(prod) ⇒
              val newProd = modifier(prod, field.originalInput, field.correctedInput filter (_.nonEmpty) map (_.toDouble) getOrElse 0.0)
              Products.commit(newProd.copy(lastModified = DateTime.now))
              onSelectionChanged()
              onProductsEdited
            case _ ⇒
          }
        }
      })
  }

  def refresh(): Unit = {
    val ConvertToBasic = "Convert to a basic product…"
    val ConvertToCompound = "Convert to a compound product…"
    product.get match {
      case Some(prod: BasicProduct) ⇒
        compoundPane.setVisible(false)
        basicPane.setVisible(true)
        nutritionalValues foreach {
          case (_, field, reader, _) ⇒
            field.reset(reader(prod))
            field.setEnabled(true)
        }
        stats.setData(prod.nutrition, NutritionalValue.PerGrams)
        convertButton.setText(ConvertToCompound)
        convertButton.setEnabled(true)
      case Some(prod: CompoundProduct) ⇒
        compoundPane.setVisible(true)
        basicPane.setVisible(false)
        massPre.reset(prod.massPreExpr)
        massPost.reset(prod.massPostExpr)
        import scala.language.reflectiveCalls
        compoundPane.setMassChange(prod.massReduction)
        ingredientsModel.fireTableDataChanged()
        stats.setData(prod.nutrition, NutritionalValue.PerGrams)
        convertButton.setText(ConvertToBasic)
        convertButton.setEnabled(true)
      case _ ⇒
        compoundPane.setVisible(false)
        basicPane.setVisible(true)
        nutritionalValues map (_._2) foreach { f ⇒ f.reset(""); f.setEnabled(false) }
        stats.setData(NutritionalValue.Zero, 0.0)
        convertButton.setText(ConvertToCompound)
        convertButton.setEnabled(false)
    }
  }

  val compoundPane = new JPanel {
    def setMassChange(mc: Double): Unit = border.setTitle(f"<html>Heating-related mass change: <b>${mc * 100.0}%.1f%%</b></html>")
    setOpaque(false)
    setLayout(new BorderLayout)
    private val border = BorderFactory.createTitledBorder("")
    setMassChange(1.0)
    private val massRed = {
      val p = new JPanel
      p.setOpaque(false)
      border.setTitleJustification(TitledBorder.CENTER)
      p.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(0, 0, 5, 0)))
      p.setLayout(new GridBagLayout)
      val c = new GridBagConstraints
      c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 5, 0, 5)
      p.add(new JLabel("A part before processing [g]:"), c)
      c.gridx = 1; c.gridy = 0; c.weightx = 1.0
      p.add(massPre, c)
      c.gridx = 0; c.gridy = 1; c.weightx = 0.0
      p.add(new JLabel("The same part after processing [g]:"), c)
      c.gridx = 1; c.gridy = 1; c.weightx = 1.0
      p.add(massPost, c)
      p
    }
    private val sp = new JScrollPane(ingredients)
    sp.setBorder(new EmptyBorder(0, 0, 10, 0))
    sp.setOpaque(false)
    add(sp, BorderLayout.CENTER)
    add(massRed, BorderLayout.PAGE_END)
  }

  val basicPane = {
    val p = new JPanel
    p.setOpaque(false)
    p.setLayout(new GridBagLayout)

    val c = new GridBagConstraints
    c.gridx = 0
    c.gridy = 0
    c.weightx = 1.0
    c.weighty = 1.0
    c.insets = new Insets(5, 0, 5, 0)
    c.fill = GridBagConstraints.BOTH
    c.gridwidth = 2
    p.add(new JLabel(f"Nutritional value in ${NutritionalValue.PerGrams}%.0f grams:"), c)
    c.gridwidth = 1

    nutritionalValues foreach {
      case (label, field, _, _) ⇒
        c.gridy += 1
        c.gridx = 0
        c.insets = new Insets(5, 15, 5, 0)
        val l = new JLabel(label)
        p.add(l, c)
        c.gridx += 1
        c.insets = new Insets(5, 0, 5, 0)
        p.add(field, c)
        field.setPreferredSize(new Dimension(150, 0))
    }
    p
  }

  layout()
  refresh()

  private[this] def layout(): Unit = {
    setLayout(new GridBagLayout)
    setOpaque(false)

    products.setFixedCellHeight(25)

    ingredients.setFillsViewportHeight(true)
    ingredients.setShowGrid(false)
    ingredients.setRowHeight(30)
    ingredients.getColumnModel.getColumn(1).setMaxWidth(60)

    val c = new GridBagConstraints
    c.gridx = 0
    c.gridy = 0
    c.insets = new Insets(5, 5, 0, 5)
    c.weightx = 1.0
    c.weighty = 0.0
    c.fill = GridBagConstraints.HORIZONTAL
    add({
      val jp = new JPanel
      jp.setOpaque(false)
      jp.setLayout(new BorderLayout)
      jp.add(filter, BorderLayout.CENTER)
      edt { addButton.setPreferredSize(new Dimension(addButton.getHeight, 0)) }
      jp.add(addButton, BorderLayout.LINE_END)
      jp
    }, c)

    c.gridy += 1
    c.insets = new Insets(0, 5, 5, 5)
    c.weighty = 1.0
    c.fill = GridBagConstraints.BOTH
    add(new JScrollPane(products), c)

    c.gridy += 1
    c.insets = new Insets(5, 5, 5, 5)
    c.weighty = 0.0
    c.fill = GridBagConstraints.HORIZONTAL
    add(stats, c)

    val editorHeight = 300

    c.gridy += 1
    basicPane.setPreferredSize(new Dimension(0, editorHeight))
    basicPane.setMinimumSize(new Dimension(0, editorHeight))
    add(basicPane, c)

    c.gridy += 1
    compoundPane.setPreferredSize(new Dimension(0, editorHeight))
    compoundPane.setMinimumSize(new Dimension(0, editorHeight))
    add(compoundPane, c)

    c.gridy += 1
    add({
      val jp = new JPanel
      jp.setOpaque(false)
      jp.setLayout(new BorderLayout)
      jp.add(convertButton, BorderLayout.LINE_END)
      jp
    }, c)

    basicPane.setVisible(true)
    compoundPane.setVisible(false)
  }

}
