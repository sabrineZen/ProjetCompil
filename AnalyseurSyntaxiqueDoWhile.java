import java.util.List;
import java.util.ArrayList;

// Grammaire 
// Z -> S #
// S -> do { Instructions } while ( E ) ;
// Instructions -> Instruction Instructions | Instruction
// Instruction -> id = Expression ; | S
// E -> id O id
// O -> < | > | == | != | <= | >=
// Expression -> Terme OpArith Terme | Terme
// Terme -> id | nombre
// OpArith -> + | - | * | /

class AnalyseurSyntaxiqueDoWhile {
    String[] tokens;
    int pos = 0;
    List<String> erreurs = new ArrayList<>();

    public AnalyseurSyntaxiqueDoWhile(List<String> tokenList) {
        if(tokenList != null) {
            this.tokens = tokenList.toArray(new String[0]);
        } else {
            this.tokens = new String[0];
        }
    }

    private boolean correspond(String expected) {
        if(pos < tokens.length && tokens[pos].equals(expected)) {
            pos++;
            return true;
        }
        return false;
    }

    private String getCurrentToken() {
        if(pos < tokens.length) {
            return tokens[pos];
        }
        return "fin";
    }

    // Z -> S #
    public boolean Z() {
        System.out.println("\n========== ANALYSE SYNTAXIQUE ==========\n");
        
        if(!S()) {
            return false;
        }
        
        // Vérifier le # final
        if(!correspond("#")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Le programme doit se terminer par '#'";
            if(pos < tokens.length) {
                erreur += " - Token trouve: '" + tokens[pos] + "'";
            }
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        return true;
    }

    // S -> do { Instructions } while ( E ) ;
    public boolean S() {
        if(!correspond("do")) {
            return false;
        }
        
        if(!correspond("{")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: '{' attendu apres 'do', trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!Instructions()) {
            return false;
        }
        
        if(!correspond("}")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: '}' attendu pour fermer le bloc, trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!correspond("while")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: 'while' attendu apres '}', trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!correspond("(")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: '(' attendu apres 'while', trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!E()) {
            return false;
        }
        
        if(!correspond(")")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: ')' attendu pour fermer la condition, trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!correspond(";")) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: ';' attendu a la fin du do-while, trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }

        return true;
    }

    // Instructions -> Instruction Instructions | Instruction
    public boolean Instructions() {
        if(!Instruction()) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Instruction attendue, trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        int savePos = pos;
        while(Instruction()) {
            savePos = pos;
        }
        pos = savePos;
        
        return true;
    }

    // Instruction -> id = Expression ; | S
    public boolean Instruction() {
        int savePos = pos;
        
        // Essayer do-while imbriqué
        if(S()) return true;
        
        // Essayer assignation: id = Expression ;
        pos = savePos;
        if(pos < tokens.length && isIdentifier(tokens[pos])) {
            pos++;
            if(correspond("=")) {
                if(Expression()) {
                    if(correspond(";")) {
                        return true;
                    } else {
                        String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: ';' attendu apres l'expression, trouve '" + getCurrentToken() + "'";
                        erreurs.add(erreur);
                        System.out.println(erreur);
                    }
                }
            }
        }
        
        pos = savePos;
        return false;
    }

    // Expression -> Terme OpArith Terme | Terme
    public boolean Expression() {
        // Il DOIT y avoir au moins un terme
        if(!Terme()) {
            return false;
        }
        
        // Sauvegarder la position avant de chercher un opérateur
        int savePos = pos;
        
        // Verifier s'il y a un operateur
        if(pos < tokens.length && OpArith()) {
            // Si on a trouvé un opérateur, il FAUT un deuxième terme
            if(!Terme()) {
                // echec : opirateur sans deuxieme terme
                String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Terme attendu apres l'operateur '" + tokens[pos-1] + "'";
                erreurs.add(erreur);
                System.out.println(erreur);
                return false;
            }
            // Succes : terme operateur terme
            return true;
        }
        
        // Pas d'operateur trouve, juste un terme (c'est OK aussi)
        return true;
    }

    // Terme -> id | nombre
    private boolean Terme() {
        if(pos >= tokens.length) {
            return false;
        }
        
        String token = tokens[pos];
        
        // Accepter un identificateur
        if(isIdentifier(token)) {
            pos++;
            return true;
        }
        
        // Accepter un nombre
        if(isNumber(token)) {
            pos++;
            return true;
        }
        
        return false;
    }

    // OpArith -> + | - | * | /
    private boolean OpArith() {
        if(pos >= tokens.length) {
            return false;
        }
        
        String t = tokens[pos];
        if(t.equals("+") || t.equals("-") || t.equals("*") || t.equals("/")) {
            pos++;
            return true;
        }
        return false;
    }

    // E -> id O id (condition stricte dans while)
    public boolean E() {
        if(pos >= tokens.length) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Condition attendue (format: id operateur id)";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!isIdentifier(tokens[pos])) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Identificateur attendu dans la condition, trouve '" + tokens[pos] + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        pos++;

        if(!O()) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Operateur de comparaison attendu (<, >, ==, !=, <=, >=), trouve '" + getCurrentToken() + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }

        if(pos >= tokens.length) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Identificateur attendu apres l'operateur";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        
        if(!isIdentifier(tokens[pos])) {
            String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Identificateur attendu apres l'operateur, trouve '" + tokens[pos] + "'";
            erreurs.add(erreur);
            System.out.println(erreur);
            return false;
        }
        pos++;

        return true;
    }

    // O -> < | > | == | != | <= | >= (opérateurs de comparaison)
    public boolean O() {
        if(pos >= tokens.length) return false;
        String t = tokens[pos];
        if(t.equals("<") || t.equals(">") || t.equals("==") || 
           t.equals("!=") || t.equals("<=") || t.equals(">=")) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean isIdentifier(String token) {
        if(token == null || token.length() == 0) return false;
        
        char premier = token.charAt(0);
        if(!((premier >= 'a' && premier <= 'z') || (premier >= 'A' && premier <= 'Z'))) {
            return false;
        }
        
        for(int i = 1; i < token.length(); i++) {
            char c = token.charAt(i);
            if(!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumber(String token) {
        if(token == null || token.length() == 0) return false;
        
        for(int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if(c == '.' && i > 0 && i < token.length() - 1) {
                continue; // Accepter le point décimal
            }
            if(!(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }

    public boolean analyser() {
        boolean ok = Z();
        
        System.out.println("\n======================================");
        if(ok && pos == tokens.length) {
            System.out.println("ANALYSE SYNTAXIQUE REUSSIE !");
            System.out.println("Le programme respecte la grammaire.");
            System.out.println("======================================\n");
            return true;
        } else {
            if(pos < tokens.length) {
                String erreur = "ERREUR SYNTAXIQUE [Position " + pos + "]: Tokens restants non traites - Token: '" + tokens[pos] + "'";
                erreurs.add(erreur);
                System.out.println(erreur);
            }
            System.out.println("\nANALYSE SYNTAXIQUE ECHOUEE !");
            System.out.println("Nombre d'erreurs: " + erreurs.size());
            System.out.println("======================================\n");
            return false;
        }
    }

}