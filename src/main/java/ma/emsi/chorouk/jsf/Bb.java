
package ma.emsi.chorouk.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.chorouk.llm.LlmClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Module de support pour la page JSF index.xhtml.
 * Portée vue pour conserver l'état de la conversation qui dure
 * pendant plusieurs requêtes HTTP.
 * La portée vue nécessite l'implémentation de Serializable.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    /**
     * Rôle "système" que l'on attribuera au LLM.
     * Possibilité d'écrire un nouveau rôle dans la liste déroulante.
     */
    private String roleSysteme;

    /**
     * Quand le rôle est choisi par l'utilisateur dans la liste déroulante,
     * il n'est plus possible de le modifier, sauf si on veut un nouveau chat.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste de tous les rôles de l'API prédéfinis.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse de l'API du LLM.
     */
    private String reponse;

    /**
     * La conversation depuis le début.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Client pour communiquer avec Gemini en utilisant LangChain4j.
     */
    @Inject
    private LlmClient llmClient;

    /**
     * Contexte JSF. Utilisé pour afficher les messages d'erreur.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Constructeur sans argument.
     */
    public Bb() {
    }

    public String getRoleSysteme() {
        return this.roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return this.roleSystemeChangeable;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return this.reponse;
    }

    /**
     * Setter indispensable pour le textarea.
     */
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie la question au serveur.
     * Le serveur utilise LangChain4j pour communiquer avec Gemini.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {

        if (this.question == null || this.question.isBlank()) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question"
            );
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Si la conversation n'a pas encore commencé
            if (this.conversation.isEmpty()) {

                // Définir le rôle système avant la première question
                llmClient.setSystemRole(this.roleSysteme);
            }

            // Envoyer la question au LLM
            this.reponse = llmClient.envoyerQuestion(this.question);

            // Figer la liste déroulante après le premier échange réussi
            this.roleSystemeChangeable = false;

            // Ajouter la question et la réponse à la conversation
            afficherConversation();

            return null;

        } catch (Exception e) {

            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Erreur lors de l'appel à Gemini",
                    e.getMessage()
            );

            facesContext.addMessage(null, message);

            return null;
        }
    }

    /**
     * Pour un nouveau chat.
     * Termine la portée view en retournant "index".
     * Une nouvelle instance du backing bean sera utilisée.
     *
     * @return "index"
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Pour afficher la conversation dans le textarea de la page JSF.
     */
    private void afficherConversation() {

        this.conversation
                .append("== Utilisateur:\n")
                .append(question)
                .append("\n== Serveur:\n")
                .append(reponse)
                .append("\n");
    }

    /**
     * Liste des rôles système prédéfinis.
     */
    public List<SelectItem> getRolesSysteme() {

        if (this.listeRolesSysteme == null) {

            this.listeRolesSysteme = new ArrayList<>();

            // Rôle Assistant
            String role = """
                    Vous êtes un assistant précieux.
                    Vous aidez l'utilisateur à trouver les informations
                    dont il a besoin.
                    Si l'utilisateur saisit une question,
                    vous y répondez.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Assistant")
            );

            // Rôle Traducteur
            role = """
                    Vous êtes interprète.
                    Vous traduisez de l'anglais vers le français
                    et du français vers l'anglais.
                    Si l'utilisateur saisit un texte en français,
                    vous le traduisez en anglais.
                    Si l'utilisateur saisit un texte en anglais,
                    vous le traduisez en français.
                    Si le texte ne contient qu'un à trois mots,
                    donnez quelques exemples d'utilisation
                    de ces mots en anglais.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Traducteur Anglais-Français")
            );

            // Rôle Guide touristique
            role = """
                    Vous êtes un guide de voyage.
                    Si l'utilisateur saisit le nom d'un pays
                    ou d'une ville, vous leur indiquez
                    les principaux endroits à visiter
                    dans le pays ou la ville.
                    Vous leur indiquez le prix moyen d'un repas.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Guide touristique")
            );
        }

        return this.listeRolesSysteme;
    }
}
