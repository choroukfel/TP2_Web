
package ma.emsi.chorouk.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.chorouk.llm.LlmClient;

import java.io.Serializable;

@Named
@ViewScoped
public class Bb implements Serializable {

    private String question;
    private String reponse;
    private String conversation = "";

    @Inject
    private LlmClient llmClient;

    public void envoyer() {
        if (question == null || question.isBlank()) {
            return;
        }

        try {
            reponse = llmClient.envoyerQuestion(question);

            conversation += "Vous : " + question + "\n";
            conversation += "Assistant : " + reponse + "\n\n";

            question = "";

        } catch (Exception e) {
            reponse = "Erreur lors de l'appel à Gemini : "
                    + e.getMessage();
        }
    }

    public void effacer() {
        question = "";
        reponse = "";
        conversation = "";
        llmClient.effacerConversation();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }
}
