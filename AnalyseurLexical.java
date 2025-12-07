import java.util.ArrayList;
import java.util.List;

class AnalyseurLexical {
    String[] motCle = {"if", "else", "then", "while", "do", "begin", "end", "int", "float", "foreach", "for"};
    String[] motClePerso = {"SABRINE"}; 
    String[] operateur = {"=", "+", "-", "*", "/", "<", ">", "<=", ">=", "==", "!="};
    String[] separateur = {",", ";", "(", ")", "{", "}"};
    String[] caracteSpeciaux = {"#", "$", "@", "&", "!"};

    int[][] table = {   
        /*0*//*etat initial */      {1, 2, 4, 4, 4, 4, 4, 5, 6},
        /*1*//*lire un id */        {1, 1, -1, -1, -1, -1, -1, -1, -1},
        /*2*//*je lis un nombre */  {-1, 2, 4, -1, -1, -1, -1, -1, -1},
        /*3*//*etat du nombre*/     {-1, 3, -1, -1, -1, -1, -1, -1, -1},
        /*4*//*operateur */         {-1, -1, -1, -1, -1, -1, -1, -1, -1},
        /*5*//*separateur */        {-1, -1, -1, -1, -1, -1, -1, -1, -1},
        /*6*//*caractere special */ {-1, -1, -1, -1, -1, -1, -1, -1, -1},
    };

    List<String> erreurs = new ArrayList<>();

    boolean estLettre(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    boolean estChiffre(char c) {
        return c >= '0' && c <= '9';
    }

    boolean estOperateur(String s) {
        for(int i = 0; i < operateur.length; i++) {
            if(operateur[i].equals(s)) return true;
        }
        return false;
    }

    boolean estMotCle(String s) {
        for(int i = 0; i < motCle.length; i++) {
            if(motCle[i].equals(s)) return true;
        }
        return false;
    }

    boolean estMotClePerso(String s) {
        for(int i = 0; i < motClePerso.length; i++) {
            if(motClePerso[i].equals(s)) return true;
        }
        return false;
    }

    boolean estSeparateur(char c) {
        for(int i = 0; i < separateur.length; i++) {
            if(separateur[i].charAt(0) == c) return true;
        }
        return false;
    }

    boolean estCaractereSpecial(char c) {
        for(int i = 0; i < caracteSpeciaux.length; i++) {
            if(caracteSpeciaux[i].charAt(0) == c) return true;
        }
        return false;
    }

    int colSymbole(char c) {
        if(estLettre(c)) return 0;
        if(estChiffre(c)) return 1;
        if(c == '=') return 2;
        if(c == '<') return 3;
        if(c == '>') return 4;
        if(c == '!') return 5;
        if(c == '+' || c == '-' || c == '*' || c == '/') return 6;
        if(estSeparateur(c)) return 7;
        if(estCaractereSpecial(c)) return 8;
        return -1;
    }

    String categoriserToken(String token) {
        if(estMotClePerso(token)) return "Mot-cle personnalise";
        if(estMotCle(token)) return "Mot-cle";
        if(estOperateur(token)) return "Operateur";
        if(token.length() == 1 && estSeparateur(token.charAt(0))) return "Separateur";
        if(token.length() == 1 && estCaractereSpecial(token.charAt(0))) return "Caractere special";
        
        // Vérifier si c'est un nombre
        boolean estNombre = true;
        boolean hasPoint = false;
        for(int i = 0; i < token.length(); i++) {
            if(token.charAt(i) == '.') {
                if(hasPoint) {
                    estNombre = false;
                    break;
                }
                hasPoint = true;
            } else if(!estChiffre(token.charAt(i))) {
                estNombre = false;
                break;
            }
        }
        if(estNombre && token.length() > 0) {
            if(hasPoint) {
                return "Nombre flottant";
            } else {
                return "Nombre entier";
            }   
        }
        
        // Vérifier si c'est un identificateur valide
        if(token.length() > 0 && estLettre(token.charAt(0))) {
            boolean valide = true;
            for(int i = 1; i < token.length(); i++) {
                if(!estLettre(token.charAt(i)) && !estChiffre(token.charAt(i))) {
                    valide = false;
                    break;
                }
            }
            if(valide) return "Identificateur";
        }
        
        return "invalide";
    }

    public List<String> analyser(String code) {
        List<String> tokens = new ArrayList<>();
        int etat = 0;
        String token = "";
        int ligne = 1;
        int colonne = 1;

        System.out.println("\n========== ANALYSE LEXICALE ==========\n");

        for(int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            
            // Vérifier si c'est le dernier caractère ET que c'est #
            if(c == '#' && i == code.length() - 1) {
                if(!token.isEmpty()) {
                    String categorie = categoriserToken(token);
                    System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
                    if(!categorie.equals("INVALIDE")) {
                        tokens.add(token);
                    }
                    token = "";
                }
                String categorie = categoriserToken("#");
                System.out.println("Lexeme: '#' \t-> Categorie: " + categorie);
                tokens.add("#");
                break;
            }

            // Gérer un  retour à la ligne
            if(c == '\n') {
                if(!token.isEmpty()) {
                    String categorie = categoriserToken(token);
                    System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
                    if(categorie.equals("INVALIDE")) {
                        String erreur = "ERREUR LEXICALE [Ligne " + ligne + ", Col " + (colonne - token.length()) + "]: Token invalide '" + token + "'";
                        erreurs.add(erreur);
                        System.out.println("  -> " + erreur);
                    } else {
                        tokens.add(token);
                    }
                    token = "";
                    etat = 0;
                }
                ligne++;
                colonne = 1;
                continue;
            }

            // GESTION DES COMMENTAIRES // (AVANT les espaces blancs)
            if(c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                // Sauvegarder le token en cours avant le commentaire
                if(!token.isEmpty()) {
                    String categorie = categoriserToken(token);
                    System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
                    if(categorie.equals("INVALIDE")) {
                        String erreur = "ERREUR LEXICALE [Ligne " + ligne + ", Col " + (colonne - token.length()) + "]: Token invalide '" + token + "'";
                        erreurs.add(erreur);
                        System.out.println(" ->" + erreur);
                    } else {
                        tokens.add(token);
                    }
                    token = "";
                    etat = 0;
                }
                
                // Ignorer tout jusqu'à la fin de la ligne
                System.out.println("[Commentaire ignore a la ligne " + ligne + "]");
                while(i < code.length() && code.charAt(i) != '\n' && code.charAt(i) != '\r') {
                    i++;
                    colonne++;
                }
                continue;
            }

            // Ignorer espaces blancs
            if(c == ' ') {
                if(!token.isEmpty()) {
                    String categorie = categoriserToken(token);
                    System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
                    if(categorie.equals("INVALIDE")) {
                        String erreur = "ERREUR LEXICALE [Ligne " + ligne + ", Col " + (colonne - token.length()) + "]: Token invalide '" + token + "'";
                        erreurs.add(erreur);
                        System.out.println("  -> " + erreur);
                    } else {
                        tokens.add(token);
                    }
                    token = "";
                    etat = 0;
                }
                colonne++;
                continue;
            }

            // Vérifier doubles opérateurs en priorité (==, !=, <=, >=)
            if(i + 1 < code.length()) {
                char nextChar = code.charAt(i + 1);
                String doubleOp = "" + c + nextChar;
                
                if(estOperateur(doubleOp)) {
                    if(!token.isEmpty()) {
                        String categorie = categoriserToken(token);
                        System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
                        if(categorie.equals("INVALIDE")) {
                            String erreur = "ERREUR LEXICALE [Ligne " + ligne + ", Col " + (colonne - token.length()) + "]: Token invalide '" + token + "'";
                            erreurs.add(erreur);
                            System.out.println("  -> " + erreur);
                        } else {
                            tokens.add(token);
                        }
                        token = "";
                        etat = 0;
                    }
                    String categorie = categoriserToken(doubleOp);
                    System.out.println("Lexeme: '" + doubleOp + "' \t-> Categorie: " + categorie);
                    tokens.add(doubleOp);
                    i++; // Sauter le caractère suivant
                    colonne += 2;
                    continue;
                }
            }

            int col = colSymbole(c);
            
            if(col == -1) {
                String erreur = "ERREUR LEXICALE [Ligne " + ligne + ", Col " + colonne + "]: Caractere invalide '" + c + "'";
                erreurs.add(erreur);
                System.out.println("  -> " + erreur);
                token = "";
                etat = 0;
                colonne++;
                continue;
            }

            int nextEtat = table[etat][col];

            if(nextEtat == -1) {
                if(!token.isEmpty()) {
                    String categorie = categoriserToken(token);
                    System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
                    if(categorie.equals("INVALIDE")) {
                        String erreur = "ERREUR LEXICALE [Ligne " + ligne + ", Col " + (colonne - token.length()) + "]: Token invalide '" + token + "'";
                        erreurs.add(erreur);
                        System.out.println("  -> " + erreur);
                    } else {
                        tokens.add(token);
                    }
                    token = "";
                }
                
                etat = table[0][col];
                if(etat != -1) {
                    token += c;
                } else {
                    String singleToken = "" + c;
                    String categorie = categoriserToken(singleToken);
                    System.out.println("Lexeme: '" + singleToken + "' \t-> Categorie: " + categorie);
                    tokens.add(singleToken);
                    etat = 0;
                }
            } else {
                token += c;
                etat = nextEtat;
            }
            
            colonne++;
        }

        if(!token.isEmpty()) {
            String categorie = categoriserToken(token);
            System.out.println("Lexeme: '" + token + "' \t-> Categorie: " + categorie);
            if(categorie.equals("INVALIDE")) {
                String erreur = "ERREUR LEXICALE: Token invalide '" + token + "'";
                erreurs.add(erreur);
                System.out.println("  >>> " + erreur);
            } else {
                tokens.add(token);
            }
        }

        System.out.println("\n======================================");
        if(erreurs.isEmpty()) {
            System.out.println("ANALYSE LEXICALE TERMINEE AVEC SUCCES!");
            System.out.println("Nombre de tokens: " + tokens.size());
        } else {
            System.out.println("ANALYSE LEXICALE TERMINEE AVEC " + erreurs.size() + " ERREUR(S)");
        }
        System.out.println("======================================\n");

        return tokens;
    }


}