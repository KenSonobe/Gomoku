import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.sound.sampled.*;

class Configure {
    // とりあえずデフォルトの値は20にしておく
    int edge = 20;
    int r_margin = 20;
    int l_margin = 20;
    int t_margin = 20;
    int b_margin = 20;

    Configure(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("設定ファイルが見つかりません．");
            System.out.println("すべてデフォルトの値を設定しました．");
            return;
        }
        try {
            Scanner fileIn = new Scanner(file);
            while (fileIn.hasNextLine()) {
                String line = fileIn.nextLine();
                // ","で区切る＆","の前後に空白が1個以上あってもOK
                String[] str = line.split("[\\s]*,[\\s]*");
                if (str.length != 2) {
                    System.out.println("解析不能な行: " + line);
                    continue;
                }
                int val = Integer.parseInt(str[1]);
                if (str[0].equalsIgnoreCase("edge")) {
                    edge = val;
                } else if (str[0].equalsIgnoreCase("r_margin")) {
                    r_margin = val;
                } else if (str[0].equalsIgnoreCase("l_margin")) {
                    l_margin = val;
                } else if (str[0].equalsIgnoreCase("t_margin")) {
                    t_margin = val;
                } else if (str[0].equalsIgnoreCase("b_margin")) {
                    b_margin = val;
                } else {
                    System.out.println("設定不能: " + str[0]);
                }
            }
            fileIn.close();
        } catch (FileNotFoundException err) {
            System.out.println("" + err);
        }
    }

    void print() {
        System.out.println("--碁盤の設定--");
        System.out.println("1マスの辺: " + edge);
        System.out.println("右マージン: " + r_margin);
        System.out.println("左マージン: " + l_margin);
        System.out.println("上マージン: " + t_margin);
        System.out.println("下マージン: " + b_margin);
        System.out.println("----");
    }
}

class Board {
    private int[][] state;
    int width;
    int height;
    private int win;
    private int num;
    int current;
    final int no_stone = 0;
    final int black = 1;
    final int white = 2;

    private boolean bEnd;
    int winner;

    Board(int w, int h, int n) {
        state = new int[w][h];
        width = w;
        height = h;
        win = n;

        num = 0;
        current = black; // 先手は黒

        winner = no_stone; // 勝者
        bEnd = false;
    }

    void setWinner(int color) {
        bEnd = true;
        winner = color;
    }

    boolean isEnd() {
        return bEnd;
    }

    boolean isFull() {
        return !(num < width * height);
    }

    void put(int x, int y, int stone) {
        state[x][y] = stone;
        num++;
    }

    boolean check(int x, int y, int stone) {
        return (checker(x, y, 1, 0, stone) || checker(x, y, 0, 1, stone) || checker(x, y, 1, 1, stone) || checker(x, y, 1, -1, stone));
    }

    private boolean checker(int x, int y, int dx, int dy, int stone) {
        int i, j;
        int count = 1;

        i = x + dx;
        j = y + dy;
        while (get(i, j) == stone) {
            count++;
            i += dx;
            j += dy;
        }

        i = x - dx;
        j = y - dy;
        while (get(i, j) == stone) {
            count++;
            i -= dx;
            j -= dy;
        }

        return (count >= win);
    }

    void turn() {
        if (current == black) {
            current = white;
        } else {
            current = black;
        }
    }

    int get(int i, int j) {
        final int error = -1;
        if (i < 0 || j < 0 || i >= width || j >= height)
            return error;
        return state[i][j];
    }
}

class SwingDraw extends JPanel {
    private MainFrame frame;
    private final int bstroke; // 線の太さ
    private final int s_margin; // マスの余白
    private Color ivory = new Color(200, 150, 80);

    SwingDraw(MainFrame f) {
        frame = f;
        bstroke = 1;
        s_margin = 2;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 背景色の設定
        setBackground(ivory);

        // ボードの描画
        Graphics2D g2 = (Graphics2D) g;
        BasicStroke w = new BasicStroke(bstroke);
        g2.setStroke(w);
        g2.setColor(Color.black);

        int dx, dy;
        // 縦の線
        for (int i = 0; i <= frame.bd.width; i++) {
            int sx = frame.cf.l_margin + frame.cf.edge * i;
            int sy = frame.cf.t_margin;
            dx = sx;
            dy = frame.cf.t_margin + frame.cf.edge * frame.bd.height;
            g2.drawLine(sx, sy, dx, dy);
        }
        // 横の線
        for (int i = 0; i <= frame.bd.height; i++) {
            int sx = frame.cf.l_margin;
            int sy = frame.cf.t_margin + frame.cf.edge * i;
            dx = frame.cf.l_margin + frame.cf.edge * frame.bd.width;
            dy = sy;
            g2.drawLine(sx, sy, dx, dy);
        }

        // 石の描画
        for (int i = 0; i < frame.bd.width; i++) {
            for (int j = 0; j < frame.bd.height; j++) {
                if (frame.bd.get(i, j) != frame.bd.no_stone) {
                    g2.setColor(Color.black);
                    int x = frame.cf.l_margin + i * frame.cf.edge + s_margin;
                    int y = frame.cf.t_margin + j * frame.cf.edge + s_margin;
                    int e = frame.cf.edge - 2 * s_margin;
                    if (frame.bd.get(i, j) == frame.bd.black) {
                        g2.fillOval(x, y, e, e);
                    } else {
                        g2.setColor(Color.white);
                        g2.fillOval(x, y, e, e);
                        g2.setColor(Color.black);
                        g2.drawOval(x, y, e, e);
                    }
                }
            }
        }

        // 勝敗決定後
        if (frame.bd.isEnd()) {
            g2.setColor(Color.red);
            g2.drawString("End", frame.cf.l_margin, frame.cf.t_margin);
        }
    }
}

class MainFrame extends JFrame {
    Configure cf; // マスなどのサイズ
    Board bd; // 碁盤の情報
    SwingDraw panel;

    int state = 0;
    int SC;

    Server sv;
    Client cl;

    MainFrame(String title, Configure c, Board b) {
        super(title);
        cf = c;
        bd = b;

        // パネルの作成＆追加
        panel = new SwingDraw(this);
        Container cp = getContentPane();
        cp.add(panel);

        // マウスリスナー
        MouseCheck ms = new MouseCheck(this);
        panel.addMouseListener(ms);

        // ウィンドウを閉じた時の処理
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // ウィンドウの幅を計算
        int width = cf.l_margin + cf.r_margin;
        width += cf.edge * bd.width;
        // ウィンドウの高さを計算
        int height = cf.t_margin + cf.b_margin;
        height += cf.edge * bd.height;
        // 枠線とタイトルバーを除いてwidth×heightとなるようにする
        Dimension d = new Dimension(width, height);
        cp.setPreferredSize(d);
        pack();
        setVisible(true);
        // 確認用の出力
        System.out.println(width + "x" + height + "pixelのウィンドウを作りました．");

        ConfigureSocket();
    }

    private void ConfigureSocket() {
        Scanner stdIn = new Scanner(System.in);

        System.out.println("ネットワーク対戦五目並べにようこそ!");
        System.out.print("接続を待ちますか？ 1:Yes, 0:No => ");
        SC = stdIn.nextInt();

        int port = 20000; //ポート番号

        switch (SC) {
            case 1:
                // サーバとして接続を待つ
                sv = new Server(port, this);
                // 接続があったら制御情報を表示
                sv.printInfo();
                RecvThread rt1 = new RecvThread(sv.sockIn, sv.sockOut, this);
                Thread th1 = new Thread(rt1);
                sv.setRt(th1);
                th1.start();
                state = 1;
            default:
                // クライアントとして接続する
                System.out.print("ホスト名orIPアドレス => ");
                String str = stdIn.next();
                cl = new Client(str, port, this);
                // 接続できたら制御情報を表示
                cl.printInfo();
                // 受信スレッド開始
                RecvThread rt0 = new RecvThread(cl.sockIn, cl.sockOut, this);
                Thread th0 = new Thread(rt0);
                cl.setRt(th0);
                th0.start();
                state = 1;
        }
    }

    void Info(String sv_host, String sv_ip, int sv_port, String cl_host, String cl_ip, int cl_port) {
        // サーバの情報出力
        System.out.println("サーバのホスト名 : " + sv_host);
        System.out.println("サーバのIPアドレス : " + sv_ip);
        System.out.println("サーバのポート番号 : " + sv_port);
        System.out.println();
        // クライアントの情報出力
        System.out.println("クライアントのホスト名 : " + cl_host);
        System.out.println("クライアントのIPアドレス : " + cl_ip);
        System.out.println("クライアントのポート番号 : " + cl_port);
        System.out.println();
    }
}

class MouseCheck implements MouseListener {
    private MainFrame frame;
    private SoundPlay sp;

    MouseCheck(MainFrame f) {
        frame = f;
        sp = new SoundPlay("put.wav");
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int b = e.getButton();

        // 勝負開始前から何もしない
        if (frame.state == 0) {
            return;
        }

        // 自分の番でなければ何もしない
        if (frame.SC == 1 && frame.bd.current == frame.bd.white) {
            return;
        }
        if (frame.SC == 0 && frame.bd.current == frame.bd.black) {
            return;
        }

        // もし別のボタンなら何もしない
        if (b != MouseEvent.BUTTON1) {
            return;
        }

        // もし決着済みならば何もしない
        if (frame.bd.isEnd()) {
            return;
        }

        // マージンを引く
        x -= frame.cf.l_margin;
        y -= frame.cf.t_margin;
        // マス目に変換する
        int px = x / frame.cf.edge;
        int py = y / frame.cf.edge;

        // 枠外なら何もしない
        if (px < 0 || px >= frame.bd.width || py < 0 || py >= frame.bd.height) {
            return;
        }

        // 座標が0未満の場合も何もしない
        if (x < 0 || y < 0) {
            return;
        }

        // 既に置かれているなら何もしない
        if (frame.bd.get(px, py) != frame.bd.no_stone) {
            return;
        }

        // 石をおく
        frame.bd.put(px, py, frame.bd.current);

        //音の再生
        sp.run();

        // 送信
        String str = "PUT ";
        str += String.valueOf(px);
        str += " ";
        str += String.valueOf(py);
        System.out.println("[SEND]" + str);

        if (frame.SC == 1) {
            frame.sv.send(str);
        } else {
            frame.cl.send(str);
        }

        if (frame.bd.check(px, py, frame.bd.current)) {
            frame.bd.setWinner(frame.bd.current);
            if (frame.bd.winner == frame.bd.black) {
                System.out.println("黒の勝ち");
            } else {
                System.out.println("白の勝ち");
            }
        } else if (frame.bd.isFull()) {
            frame.bd.setWinner(frame.bd.no_stone);
            System.out.println("引き分け");
        } else {
            frame.bd.turn();
        }

        // 再描画
        frame.panel.repaint();
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {

    }
}

class SoundPlay implements Runnable {
    private Clip clip = null;

    SoundPlay(String filename) {
        File file = new File(filename);
        AudioFormat format;
        DataLine.Info info;

        try {
            format = AudioSystem.getAudioFileFormat(file).getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(AudioSystem.getAudioInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        clip.setFramePosition(0);
        clip.start();
    }
}

class Server {
    private ServerSocket ss; // サーバソケット
    private Socket socket; // 通信用ソケット
    private String cl_host; // クライアントのホスト名の文字列
    private String cl_ip; // クライアントのホスト名のIPアドレス
    private int cl_port; // クライアントのポート番号
    private String sv_host; // サーバのホスト名
    private String sv_ip; // サーバのホスト名のIPアドレス
    private int sv_port; // サーバのポート番号
    Scanner sockIn; // ソケットからの入力
    PrintWriter sockOut; // ソケットへの出力
    private Thread rt;
    private MainFrame frame;

    Server(int port, MainFrame f) {
        InetAddress sv_addr; // サーバのIPアドレス
        InetAddress cl_addr; // クライアントのIPアドレス

        frame = f;
        this.sv_port = port;
        try {
            // サーバソケットの作成
            ss = new ServerSocket(port);
            // 接続を待つ(新しいディスクリプタはsocketへ代入)
            socket = ss.accept();
            // 接続があったらクライアントのIPアドレスを取得してcl_addrに代入
            cl_addr = socket.getInetAddress();
            // クライアントのホスト名を文字列で取得しcl_hostへ代入
            cl_host = cl_addr.getHostName();
            // ついでにIPアドレスも文字列で取得しcl_ipへ代入
            cl_ip = cl_addr.getHostAddress();
            // クライアントのポート番号も取得しcl_portへ代入
            cl_port = socket.getPort();
            // サーバのIPアドレスを取得してsv_addrに代入
            sv_addr = socket.getLocalAddress();
            // サーバのホスト名を文字列で取得しsv_hostへ代入
            sv_host = sv_addr.getHostName();
            // ついでにIPアドレスも文字列で取得しsv_ipへ代入
            sv_ip = sv_addr.getHostAddress();
            // 入力ストリームの準備(sockInを用いて受信できるようにする)
            InputStream ist = socket.getInputStream();
            InputStreamReader istr = new InputStreamReader(ist);
            sockIn = new Scanner(istr);
            // 出力ストリームの準備(sockOutを用いて送信できるようにする)
            OutputStream ost = socket.getOutputStream();
            sockOut = new PrintWriter(ost);
        } catch (IOException e) {
            System.out.println("" + e);
        }
        rt = null;
    }

    void printInfo() {
        frame.Info(sv_host, sv_ip, sv_port, cl_host, cl_ip, cl_port);
    }

    void setRt(Thread rt) {
        this.rt = rt;
    }

    void send(String str) {
        sockOut.println(str);
        sockOut.flush(); // こいつを実行すると送信される
    }

    void end() {
        try {
            // ソケットとかを閉じる
            sockIn.close();
            sockOut.close();
            socket.close();
            ss.close();
        } catch (IOException e) {
            System.out.println("" + e);
        }
    }
}

class Client {
    private Socket socket; // 通信用ソケット
    private int sv_port; // サーバのポート番号
    private String sv_host; // サーバのホスト名の文字列
    private String sv_ip; // サーバのホスト名のIPアドレス
    private int cl_port; // クライアントのポート番号
    private String cl_host; // クライアントのホスト名の文字列
    private String cl_ip; // クライアントのホスト名のIPアドレス
    Scanner sockIn; // ソケットからの入力
    PrintWriter sockOut; // ソケットへの出力
    private Thread rt;
    private MainFrame frame;

    Client(String host, int port, MainFrame f) {
        InetAddress sv_addr; // サーバのIPアドレス
        InetAddress cl_addr; // クライアントのIPアドレス

        this.sv_port = port;
        this.sv_host = host;
        frame = f;
        try {
            // ホスト名(or IPアドレス)の文字列から
            // サーバのIPアドレスをホスト名で取得してsv_addrへ代入する
            sv_addr = InetAddress.getByName(host);
            // クライアントソケットの作成(ディスクリプタはsocketへ代入する)
            socket = new Socket(sv_addr, this.sv_port);
            // サーバのホスト名をsv_hostへ代入する
            sv_host = host;
            // ついでにサーバのIPアドレスも文字列で取得しsv_ipへ代入する
            sv_ip = sv_addr.getHostAddress();
            // サーバのポート番号も取得しsv_portへ代入する
            sv_port = socket.getPort();
            // クライアントのIPアドレスを取得してcl_addrに代入
            cl_addr = socket.getLocalAddress();
            // サーバのホスト名を文字列で取得しsv_hostへ代入
            cl_host = cl_addr.getHostName();
            // ついでにIPアドレスも文字列で取得しsv_ipへ代入
            cl_ip = cl_addr.getHostAddress();
            // クライアントのポート番号を取得しcl_portへ代入
            cl_port = socket.getLocalPort();
            // 入力ストリームの準備(sockInを用いて受信できるようにする)
            InputStream ist = socket.getInputStream();
            InputStreamReader istr = new InputStreamReader(ist);
            sockIn = new Scanner(istr);
            // (24)出力ストリームの準備(sockOutを用いて送信できるようにする)
            OutputStream ost = socket.getOutputStream();
            sockOut = new PrintWriter(ost);
        } catch (IOException e) {
            System.out.println("" + e);
        }
        rt = null;
    }

    void printInfo() {
        frame.Info(sv_host, sv_ip, sv_port, cl_host, cl_ip, cl_port);
    }

    void setRt(Thread rt) {
        this.rt = rt;
    }

    void send(String str) {
        sockOut.println(str);
        sockOut.flush();
    }

    void end() {
        try {
            // ソケットとかを閉じる
            sockIn.close();
            sockOut.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("" + e);
        }
    }
}

class RecvThread implements Runnable {
    private Scanner sockIn;
    private PrintWriter sockOut;
    private MainFrame frame;

    RecvThread(Scanner sockIn, PrintWriter sockOut, MainFrame f) {
        this.sockIn = sockIn;
        this.sockOut = sockOut;
        frame = f;
    }

    public void run() {
        String str = sockIn.nextLine();
        String[] s = str.split(" ");
        System.out.println("[RECV] " + str);

        while (!s[0].equals("end.") && frame.state != 2) {
            // 考えるところ→
            if (s[0].equals("ERROR")) {
                sockOut.println("end.");
                frame.state = 2;
                sockOut.flush();
            } else if ((frame.SC == 1 && frame.bd.current == frame.bd.white) || (frame.SC == 0 && frame.bd.current == frame.bd.black)) {

                int x = Integer.parseInt(s[1]);
                int y = Integer.parseInt(s[2]);

                if (x < 0 || x >= frame.bd.width || y < 0 || y >= frame.bd.height) {
                    sockOut.println("ERROR");
                    frame.state = 2;
                    sockOut.flush();
                } else if (frame.bd.get(x, y) != frame.bd.no_stone) {
                    sockOut.println("ERROR");
                    frame.state = 2;
                    sockOut.flush();
                } else {
                    frame.bd.put(x, y, frame.bd.current);

                    if (frame.bd.check(x, y, frame.bd.current)) {
                        sockOut.println("end.");
                        frame.state = 2;
                    } else {
                        frame.bd.turn();
                    }
                }
            } else {
                sockOut.println("ERROR");
                frame.state = 2;
            }
            frame.panel.repaint();

            // 受信する
            str = sockIn.nextLine();
            s = str.split(" ");
            System.out.println("[RECV] " + str);
        }

        if (frame.state != 2) {
            System.out.println("[SEND] end.");
            sockOut.println("end. ");
            sockOut.flush();
        }
        System.out.println("受信スレッド終了");
    }
}


public class Gomoku {
    public static void main(String[] args) {
        // 設定ファイルから情報を読み取る
        Configure cf = new Configure("config.txt");
        // 確認のために出力してみる
        cf.print();

        // ゲームの設定 (20x20の五目並べ)
        int n = 5, w = 16, h = 16;
        // 碁盤を作成
        Board bd = new Board(w, h, n);

        // ウィンドウの作成
        String title = String.valueOf(w);
        title += "x";
        title += String.valueOf(h);
        title += "のボードで";
        title += String.valueOf(n);
        title += "目並べ";
        new MainFrame(title, cf, bd);
    }
}
