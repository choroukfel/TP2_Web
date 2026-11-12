
package ma.emsi.chorouk.llm;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    private transient Assistant assistant;

    public LlmClient() {
        initialiserAssistant();
    }

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

        assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(
                        MessageWindowChatMemory.withMaxMessages(20)
                )
                .build();
    }

    public String envoyerQuestion(String question) {
        if (assistant == null) {
            initialiserAssistant();
        }

        return assistant.chat(question);
    }

    public void effacerConversation() {
        initialiserAssistant();
    }
}
