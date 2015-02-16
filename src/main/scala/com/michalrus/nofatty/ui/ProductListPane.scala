package com.michalrus.nofatty.ui

import java.awt._
import java.util.concurrent.atomic.AtomicReference
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener }
import javax.swing.table.DefaultTableCellRenderer

import com.michalrus.nofatty.data._
import com.michalrus.nofatty.ui.utils.{ VerifyingTextField, CalculatorTextfield, FilteringListCellRenderer }

class ProductListPane extends JPanel {

  val filter = new JTextField

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
    override def getElementAt(index: Int): String = Products.names.keySet.filter(predicate).toVector.sorted.apply(index)
  }

  val product = new AtomicReference[Option[Product]](None)

  def onSelectionChanged(): Unit = {
    product.set(Option(products.getSelectedValue) flatMap Products.names.get flatMap Products.find)
    refresh()
  }

  val products: JList[String] = new JList(productsModel)
  products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  products.setCellRenderer(FilteringListCellRenderer(filter.getText))
  products.getSelectionModel.addListSelectionListener(new ListSelectionListener {
    override def valueChanged(e: ListSelectionEvent): Unit = onSelectionChanged()
  })

  val stats = new StatsPane

  val ingredients = {
    val t = new JTable(Array(Array[AnyRef]("Cow butter", "13.4")), Array[AnyRef]("Ingredient", "Grams"))
    t.getColumnModel.getColumn(1).setCellRenderer({
      val r = new DefaultTableCellRenderer
      r.setHorizontalAlignment(SwingConstants.RIGHT)
      r
    })
    t.setCellSelectionEnabled(true)
    t
  }

  val kcal, protein, fat, carbohydrate, fiber = CalculatorTextfield("", _ >= 0.0, allowEmpty = true)

  def refresh(): Unit =
    product.get match {
      case Some(prod: BasicProduct) ⇒
        compoundPane.setVisible(false)
        basicPane.setVisible(true)
        Map[VerifyingTextField, BasicProduct ⇒ String](
          kcal → (_.kcalExpr), protein → (_.proteinExpr), fat → (_.fatExpr),
          carbohydrate → (_.carbohydrateExpr), fiber → (_.fiberExpr)) foreach {
            case (f, v) ⇒
              f.reset(v(prod))
              f.setEnabled(true)
          }
        stats.setData(prod.nutrition)
      case Some(prod: CompoundProduct) ⇒
        compoundPane.setVisible(true)
        basicPane.setVisible(false)
        stats.setData(prod.nutrition)
      case _ ⇒
        compoundPane.setVisible(false)
        basicPane.setVisible(true)
        Set(kcal, protein, fat, carbohydrate, fiber) foreach { f ⇒ f.reset(""); f.setEnabled(false) }
        stats.setData(NutritionalValue.Zero)
    }

  val compoundPane = new JScrollPane(ingredients)

  val basicPane = {
    val p = new JPanel
    p.setOpaque(false)
    p.setLayout(new GridBagLayout)

    val c = new GridBagConstraints
    c.gridx = 0
    c.gridy = 0
    c.weightx = 1.0
    c.weighty = 1.0
    c.insets = new Insets(5, 5, 5, 5)
    c.fill = GridBagConstraints.BOTH
    c.gridwidth = 2
    p.add(new JLabel("Nutritional values in 100 grams:"), c)
    c.gridwidth = 1

    Seq("Kilocalories:" → kcal, "Protein:" → protein, "Fat:" → fat, "Carbohydrate:" → carbohydrate, "Fiber:" → fiber) foreach {
      case (label, field) ⇒
        c.gridy += 1
        c.gridx = 0
        c.insets = new Insets(5, 15, 5, 5)
        val l = new JLabel(label)
        //        l.setPreferredSize(new Dimension(100, 0))
        p.add(l, c)
        c.gridx += 1
        c.insets = new Insets(5, 5, 5, 5)
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
    add(filter, c)

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

    c.gridy += 1
    basicPane.setPreferredSize(new Dimension(0, 250))
    add(basicPane, c)

    c.gridy += 1
    compoundPane.setPreferredSize(new Dimension(0, 250))
    add(compoundPane, c)

    basicPane.setVisible(true)
    compoundPane.setVisible(false)
  }

}
