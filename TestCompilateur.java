import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;


public class TestCompilateur {
      public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        AnalyseurLexical lexical = new AnalyseurLexical();
        System.out.println("Mini Compilateur - Automate complet");
        System.out.println("Entrez votre code : ");
        String code = sc.nextLine()+"#";
   List<String> tokens = lexical.analyser(code);
   
        
        // . Afficher les tokens (optionnel)
        System.out.println("Tokens trouvés : " + tokens);

        
        // . Vérifier que les tokens ne sont pas vides
        if(tokens == null || tokens.isEmpty()) {
            System.out.println("Erreur : aucun token trouvé");
            return;
        }
        
        // . Créer et lancer l'analyseur syntaxique
        AnalyseurSyntaxiqueDoWhile syntaxique = new AnalyseurSyntaxiqueDoWhile(tokens);
        syntaxique.analyser();
}

}