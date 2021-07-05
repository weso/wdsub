package es.weso.shexs;

import cats.effect.IO;
import java.io.BufferedReader;
import com.google.common.collect.Maps;
import es.weso.rdf.jena.RDFAsJenaModel;
import es.weso.rdf.nodes.IRI;
import es.weso.shapemaps.ResultShapeMap;
import es.weso.shex.ResolvedSchema;
import es.weso.shex.Schema;
import es.weso.shex.validator.ExternalResolver;
import es.weso.shex.validator.NoAction;
import es.weso.shex.validator.Result;
import es.weso.shex.validator.Validator;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileStd;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;
import scala.Console;
import scala.Option;
import scala.collection.immutable.HashMap;
import es.weso.shexs.ShExS;
import es.weso.shexs.ShExsOptions;
import es.weso.shapemaps.Conformant ;

public class FhirTest3 {

  @Test
  public void testShex() {
   Console.withOut(
        OutputStream.nullOutputStream(), 
        () -> { 
            try {
        run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
     return null;
    }); 
  }

  public void run() throws Exception {
    String shex = "PREFIX fhir: <http://hl7.org/fhir/>\n"
        + "PREFIX fhirvs: <http://hl7.org/fhir/ValueSet/>\n"
        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
        + "BASE <http://hl7.org/fhir/shape/>\n"
        + "\n"
        + "start=@<All>\n"
        + "\n"
        + "<http://hl7.org/fhirpath/System.String> .\n"
        + "<http://hl7.org/fhirpath/System.Integer> .\n"
        + "<http://hl7.org/fhirpath/System.Date> .\n"
        + "<http://hl7.org/fhirpath/System.Boolean> .\n"
        + "<http://hl7.org/fhirpath/System.Time> .\n"
        + "<http://hl7.org/fhirpath/System.Decimal> .\n"
        + "<http://hl7.org/fhirpath/System.DateTime> .\n"
        + "\n"
        + "# Tracks balance, charges, for patient or cost center\n"
        + "<Account> CLOSED {\n"
        + "    a [fhir:Account];\n"
        + "    fhir:nodeRole [fhir:treeRoot]?;\n"
        + "    fhir:Account.status @<code> AND\n"
        + "    \t{fhir:value @fhirvs:account-status};\n"
        + "}\n"
        + "\n"
        + "<code> CLOSED {\n"
        + "    fhir:Element.id @<http://hl7.org/fhirpath/System.String>?;\n"
        + "    fhir:Element.extension @<Extension>*;\n"
        + "    fhir:value @<http://hl7.org/fhirpath/System.String>?;\n"
        + "    fhir:index xsd:integer?                 # Relative position in a list\n"
        + "}\n"
        + "\n"
        + "fhirvs:account-status [\"active\" \"inactive\" \"entered-in-error\" \"on-hold\" \"unknown\"]";


    String rdf =
        "<http://hl7.org/fhir/Account/example> <http://hl7.org/fhir/nodeRole> <http://hl7.org/fhir/treeRoot> .\n"
            + "<http://hl7.org/fhir/Account/example> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://hl7.org/fhir/Account> . \n"
            + "<http://hl7.org/fhir/Account/example> <http://hl7.org/fhir/Account.status> _:b14 . \n"
            + "_:b14 <http://hl7.org/fhir/value> \"active999999999999\" ."; // this is an error

    ShExsOptions options = ShExsOptions.defaultOptions();
    

    ResultShapeMap map = 
      ShExS.validateNodeShape(
        rdf,
        shex,
        "http://hl7.org/fhir/Account/example", 
        "http://hl7.org/fhir/shape/Account", 
        options);
    System.out.println("Errors ================================");
    map.associations().toList().toStream().foreach(assoc -> {
        System.out.println(assoc.info());
      // is there a way to map these to the above 'lineNumbers' map?
      // somehow I'd like to produce an error message like: "active999999999999 on line 4 of the RDF is invalid"
      return null;
    });
    System.out.println(map); 

  }


}