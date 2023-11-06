import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        LZW lzw = new LZW();
        Scanner sc = new Scanner(System.in);
        System.out.println("Please Enter Input File");
        String input = sc.nextLine();
        System.out.println("Please Enter Output File");
        String output = sc.nextLine();

        System.out.println("1) Compress\n2) Decompress");
        String option = sc.nextLine();
        switch (option) {
            case "1":
                lzw.compress(input, output);
                break;
            case "2":
                lzw.decompress(input, output);
                break;
            default:
                System.out.println("Invalid input!");
                break;
        }
        sc.close();
    }
}
