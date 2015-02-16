package com.michalrus.nofatty.ui

import java.awt._
import javax.swing._
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.table.DefaultTableCellRenderer

import com.michalrus.nofatty.data.Products
import com.michalrus.nofatty.ui.utils.FilteringListCellRenderer

class ProductListPane extends JPanel {

  val filter = new JTextField

  filter.getDocument.addDocumentListener(new DocumentListener {
    import language.reflectiveCalls
    override def insertUpdate(e: DocumentEvent): Unit = productsModel.refresh()
    override def changedUpdate(e: DocumentEvent): Unit = productsModel.refresh()
    override def removeUpdate(e: DocumentEvent): Unit = productsModel.refresh()
  })

  val productsModel = new AbstractListModel[String] {
    def refresh(): Unit = fireContentsChanged(this, 0, Int.MaxValue)
    val predicate: String ⇒ Boolean = _.toLowerCase contains filter.getText.toLowerCase
    override def getSize: Int = Products.names.keySet count predicate
    override def getElementAt(index: Int): String = Products.names.keySet.filter(predicate).toVector.sorted.apply(index)
  }

  val products = new JList(productsModel)
  products.setCellRenderer(FilteringListCellRenderer(filter.getText))

  val stats = new StatsPane

  val ingredients = {
    val t = new JTable(Array(Array[AnyRef]("Cow butter", "13.4")), Array[AnyRef]("Ingredient", "Grams"))
    t.getColumnModel.getColumn(1).setCellRenderer({
      val r = new DefaultTableCellRenderer
      r.setHorizontalAlignment(SwingConstants.RIGHT)
      r
    })
    t
  }

  val name, kcal, protein, fat, carbohydrate, fiber = new JTextField

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
