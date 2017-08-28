package c;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class Frame extends JFrame{

    JPanel panel = new JPanel(new GridBagLayout());
    JLabel label = new JLabel(" ");
    JButton start = new JButton("Старт");
    JButton impor = new JButton("Импорт");

    DefaultTableModel model = new DefaultTableModel(0, 4);  //Позволит нам изменять таблицу
    JTable table = new JTable(model);  //Приписываем сюда модель
    JScrollPane tableContainer = new JScrollPane(table);

    JFileChooser fileopen = new JFileChooser();

    private static final String SPACE = "   ";

    public void Tablica(){

        setSize(500,430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        getContentPane().setLayout(new FlowLayout());

        tableContainer.setPreferredSize(new Dimension(400, 300));  //Задаем размеры скролл-поля

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tableContainer, c);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(label, c);
        c.gridx = 0;
        c.gridy = 2;
        panel.add(start, c);
        c.gridx = 0;
        c.gridy = 3;
        panel.add(impor, c);
        add(panel);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Document doc = getDocument();
                    showDocument(doc);
                } catch (Exception ex) {
                }
            }
        });

        impor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                fileopen.setDialogTitle("Открыть файл");

                //Применяем фильтр: видит только xml-файлы
                FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
                fileopen.setFileFilter(xmlfilter);

                int ret = fileopen.showDialog(null, "Выбрать");

                if (ret == JFileChooser.APPROVE_OPTION) {  //APPROVE_OPTION - выбор файла в диалоговом окне прошел успешно

                    File file = fileopen.getSelectedFile();

                    if (file.getName()!=null){

                        try {
                            Document doc = getDocument();
                            showDocument(doc);
                        } catch (Exception ex) {
                        }
                    } else {
                        System.out.println("Что-то пошло не так");
                    }
                }
            }
        });

    }

    /**Достаем наш документ*/
    public Document getDocument() throws Exception {
        try {

            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setValidating(false);
            DocumentBuilder builder = f.newDocumentBuilder();
            return builder.parse(new File(String.valueOf(fileopen.getSelectedFile())));
        } catch (Exception exception) {
            throw new Exception("XML parsing error!");
        }
    }

    /**Показываем наш документ*/
    public void showDocument(Document doc) {

        model.setRowCount(0);  //На входе очищает таблицу от предыдущих данных, чтобы не плодить пустые строки

        StringBuffer contentClass = new StringBuffer();  //стринговые, строки
        StringBuffer contentMethod = new StringBuffer();
        Node node = doc.getChildNodes().item(0);  //элементы
        ApplicationNode appNode = new ApplicationNode(node);  //вытаскивает

        int n = 0;  //Счетчик для строк

        java.util.List<ClassNode> classes = appNode.getClasses();  //получение классов

        for (int i = 0; i < classes.size(); i++) {

            model.addRow(new Object[]{});  //Добавляем строки при нажатии кнопки

            ClassNode classNode = classes.get(i);

            model.setValueAt(classNode.getName(), i, 0);

            java.util.List<MethodNode> methods = classNode.getMethods();  //Получение элементов, методов

            for (int z = 1; z<methods.size()+1; z++) {  //Задаем еще один цикл для вывода методов XML по столбцам
                MethodNode methodNode = methods.get(z-1);
                model.setValueAt(methodNode.getName(), n, z);
            }
            n++;  //Увеличиваем счетчик на +1, чтобы строка заполнилась следующими данными
        }
    }

    /**Объектное представление приложения*/
    public static class ApplicationNode{

        Node node;

        public ApplicationNode(Node node) {
            this.node = node;
        }

        public java.util.List<ClassNode> getClasses() {
            ArrayList<ClassNode> classes = new ArrayList<ClassNode>();

            NodeList classNodes = node.getChildNodes();

            for (int i = 0; i < classNodes.getLength(); i++) {
                Node node = classNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    ClassNode classNode = new ClassNode(node);
                    classes.add(classNode);
                }
            }
            return classes;
        }
    }

    /**Работаем с классами*/
    public static class ClassNode {  //Берем класс (по аналогии можно брать и методы)

        Node node;

        public ClassNode(Node node) {
            this.node = node;
        }

        public java.util.List<MethodNode> getMethods() {
            ArrayList<MethodNode> methods = new ArrayList<MethodNode>();

            NodeList methodNodes = node.getChildNodes();  //Получаем дочерние атрибуты

            for (int i = 0; i < methodNodes.getLength(); i++) {
                node = methodNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    MethodNode methodNode = new MethodNode(node);
                    methods.add(methodNode);
                }
            }

            return methods;
        }

        public String getName() {

            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("name");
            return nameAttrib.getNodeValue();
        }
    }


    /**Работаем с методами*/
    public static class MethodNode {

        Node node;

        public MethodNode(Node node) {
            this.node = node;
        }

        public String getName() {

            NamedNodeMap attributes = node.getAttributes();  //получаем атрибуты
            Node nameAttrib = attributes.getNamedItem("name");
            return nameAttrib.getNodeValue();
        }
    }
}
