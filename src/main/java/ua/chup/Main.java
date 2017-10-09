package ua.chup;

import java.sql.*;
import java.util.*;


public class Main {
    private static List<String> regions = new ArrayList<>();
    private static Map<Integer, String> addresses = new HashMap<>();

    static {
        for (int i = 1; i < 126; i++)
            addresses.put(i, "5 Mayorova Avenue, Apt. " + i + " Kiev");
    }

    static {
        regions.add("Obolon");
        regions.add("Svyatoshin");
        regions.add("Podol");
        regions.add("Pechersk");
        regions.add("Darnitca");
    }

    private static Double square;
    private static Double koffOneRoom = 1.0;
    private static Double koffTwoRoom = 0.8;
    private static Double koffThreeRoom = 0.65;
    private static Double koffFourRoom = 0.58;


    static final String DB_CONNECTION = "jdbc:mysql://10.91.63.108:3306/cadb";
    static final String DB_USER = "monty";
    static final String DB_PASSWORD = "totoadmin";

    static Connection conn;


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            try {
                conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);

                initDB();
                System.out.println(getRandomRegion());
                while (true) {
                    System.out.println("1: recreate table Appartaments");
                    System.out.println("2: select appartaments where params");
                    System.out.print("-> ");
                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            recreateDB();
                            break;
                        case "2":
                            selectParams(sc);
                            break;

                        default:
                            return;
                    }
                }

            } finally {
                sc.close();
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }


    }

    private static void selectParams(Scanner sc) throws SQLException {
        System.out.println("Enter params, DEFAULT ALL");
        System.out.println("Enter name, DEFAULT ALL");
        String name = sc.nextLine();
        if(name.equals(""))name="";else name=" NAME='"+name+"' AND";
        System.out.println("Enter address, DEFAULT ALL");
        String address = sc.nextLine();
        if(address.equals(""))address="";else address=" ADDRESS='"+address+"' AND";
        System.out.println("Enter region, DEFAULT ALL");
        String region = sc.nextLine();
        if(region.equals(""))region="";else region=" REGION='"+region+"' AND";
        System.out.println("Enter square, DEFAULT ALL");
        String square = sc.nextLine();
        if(square.equals(""))square="";else square=" SQUARE='"+square+"' AND";
        System.out.println("Enter rooms, DEFAULT ALL");
        String rooms = sc.nextLine();
        if(rooms.equals("")) rooms="";else rooms=" ROOMS='"+rooms+"' AND";
        System.out.println("Enter price, DEFAULT ALL");
        String price = sc.nextLine();
        if(price.equals(""))price="";else price=" PRICE='"+price+"'";



        String quary = "SELECT * FROM Appartaments WHERE"+name+address+region+square+rooms+price+";";

        if(quary.substring(quary.length()-6).equals("WHERE;"))
        quary="SELECT * FROM Appartaments;";

        if(quary.substring(quary.length()-4).equals("AND;"))
        quary=quary.substring(0,quary.length()-4);


        PreparedStatement ps = conn.prepareStatement(quary);
        try{
            ResultSet rs = ps.executeQuery();
            try {
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            } finally {
                rs.close();
            }
        }finally {
            ps.close();
        }


    }


    private static void recreateDB() throws SQLException {
        Statement st = conn.createStatement();
        try {

            st.execute("DROP TABLE IF EXISTS Appartaments");

            st.execute("CREATE TABLE Appartaments (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) NOT NULL, address VARCHAR(40) NOT NULL," +
                    "region VARCHAR(20) NOT NULL, square DOUBLE, rooms VARCHAR(20) NOT NULL, price DOUBLE)");
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Appartaments (id, name,address,region,square,rooms,price) VALUES(?,?,?,?,?,?,?)");
            try {

                Rooms rooms;
                double square;
                for (int i = 1; i < addresses.size(); i++) {
                    square = getRandomSquare();
                    rooms = getRoom(square);
                    ps.setInt(1, i);
                    ps.setString(2, "App" + i);
                    ps.setString(3, addresses.get(i));
                    ps.setString(4, getRandomRegion());
                    ps.setDouble(5, square);
                    ps.setString(6, rooms.toString());
                    ps.setDouble(7, getRandomPrice(square, rooms));
                    ps.execute();
                }
            } finally {
                ps.close();
            }


        } finally {
            st.close();
        }
    }

    private static void initDB() throws SQLException {
        PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'cadb' AND table_name = 'Appartaments';");
        try {
            ResultSet check = st.executeQuery();
            check.next();
            Integer k = Integer.parseInt(check.getString(1));
            if (k == 0) {
                st.execute("CREATE TABLE Appartaments (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) NOT NULL, address VARCHAR(40) NOT NULL," +
                        "region VARCHAR(20) NOT NULL, square DOUBLE, rooms VARCHAR(20) NOT NULL, price DOUBLE)");
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Appartaments (id, name,address,region,square,rooms,price) VALUES(?,?,?,?,?,?,?)");
                try {

                    Rooms rooms;
                    double square;
                    for (int i = 1; i < addresses.size(); i++) {
                        square = getRandomSquare();
                        rooms = getRoom(square);
                        ps.setInt(1, i);
                        ps.setString(2, "App" + i);
                        ps.setString(3, addresses.get(i));
                        ps.setString(4, getRandomRegion());
                        ps.setDouble(5, square);
                        ps.setString(6, rooms.toString());
                        ps.setDouble(7, getRandomPrice(square, rooms));
                        ps.execute();
                    }
                } finally {
                    ps.close();
                }
            }
        } finally {
            st.close();
        }
    }


    private static String getRandomRegion() {
        return regions.get((int) (Math.random() * 5));
    }

    private static Double getRandomSquare() {
        return 52.6 + (Math.random() * 50);
    }

    private static Rooms getRoom(Double square) {
        if (square < 60)
            return Rooms.ONE;
        else if (square < 80)
            return Rooms.TWO;
        else if (square < 95)
            return Rooms.THREE;
        else return Rooms.FOUR;
    }

    private static Double getRandomPrice(Double square, Rooms rooms) {
        Double koff = 0.0;
        if (rooms == Rooms.ONE)
            koff = koffOneRoom;
        else if (rooms == Rooms.TWO)
            koff = koffTwoRoom;
        else if (rooms == Rooms.THREE)
            koff = koffThreeRoom;
        else koff = koffFourRoom;
        return 20000 * koff * square;
    }


}
