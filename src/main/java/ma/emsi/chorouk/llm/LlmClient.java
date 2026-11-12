
package ma.emsi.chorouk.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    /**
     * Rôle système choisi par l'utilisateur.
     */
    private String systemRole;

    /**
     * Assistant IA créé par LangChain4j.
     */
    private transient Assistant assistant;

    /**
     * Mémoire utilisée pour conserver la conversation.
     */
    private transient ChatMemory chatMemory;

    public LlmClient() {
        initialiserAssistant();
    }

    /**
     * Initialise le modèle Gemini, la mémoire et l'assistant.
     */
    private void initialiserAssistant() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable GEMINI_API_KEY n'est pas configurée."
            );
        }

        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.3)
                .build();

        // Mémoire de conversation : 20 messages maximum
        this.chatMemory =
                MessageWindowChatMemory.withMaxMessages(20);

        // Création du service IA
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définit le rôle système de l'assistant.
     * Le rôle est ajouté à la mémoire sous forme de SystemMessage.
     */
    public void setSystemRole(String systemRole) {

        if (assistant == null || chatMemory == null) {
            initialiserAssistant();
        }

        this.systemRole = systemRole;

        // Vider la mémoire avant de changer le rôle
        chatMemory.clear();

        // Ajouter le rôle système dans la mémoire
        if (systemRole != null && !systemRole.isBlank()) {
            chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    /**
     * Envoie une question au LLM et retourne sa réponse.
     */
    public String envoyerQuestion(String question) {

        if (assistant == null || chatMemory == null) {
            initialiserAssistant();

            if (systemRole != null && !systemRole.isBlank()) {
                chatMemory.add(SystemMessage.from(systemRole));
            }
        }

        return assistant.chat(question);
    }

    /**
     * Efface la conversation et réinitialise l'assistant.
     */
    public void effacerConversation() {
        this.systemRole = null;
        initialiserAssistant();
    }
}
