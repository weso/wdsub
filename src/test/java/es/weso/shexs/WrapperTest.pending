package es.weso.shexs;
import es.weso.shex.* ;
import org.apache.jena.rdf.model.*;
import org.junit.Test;
import java.io.InputStream ;
import java.io.ByteArrayInputStream;
import es.weso.shapemaps.ResultShapeMap;

public class WrapperTest {

  @Test
  public void testWrapper() {
    String schemaStr = "prefix : <http://example.org/>\n" +
                    ":S { :p . } " ;
    String dataStr = "prefix : <http://example.org/>\n" +
                  ":x :p 1 . " ;                 
    ValidatorWrapper vw = new ValidatorWrapper();
    vw.parseSchema(schemaStr, "ShEXC");
    Model dataModel = ModelFactory.createDefaultModel();
    InputStream dataStream = new ByteArrayInputStream(dataStr.getBytes());
    dataModel.read(dataStream,"");
    ResultShapeMap result = vw.validate(dataModel);
  }

}