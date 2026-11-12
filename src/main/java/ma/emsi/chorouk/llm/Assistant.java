
package ma.emsi.chorouk.llm;

/**
 * Interface du service IA.
 * LangChain4j fournit automatiquement son implémentation.
 */
public interface Assistant {

    String chat(String prompt);

}
