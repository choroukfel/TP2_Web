
package ma.emsi.chorouk.llm;

import dev.langchain4j.service.SystemMessage;

public interface Assistant {

    @SystemMessage("""
            Tu es un assistant IA serviable.
            Tu réponds aux questions en français.
            Tu donnes des réponses claires et précises.
            """)
    String chat(String question);
}
