package es.weso.shexs;

import es.weso.rdf.nodes.IRI;
import es.weso.rdf.rdf4j.RDFAsRDF4jModel;
import es.weso.shapemaps.ResultShapeMap;
import es.weso.shex.ResolvedSchema;
import es.weso.shex.Schema;
import es.weso.shex.validator.Result;
import es.weso.shex.validator.Validator;
import java.io.OutputStream;
import java.io.StringReader;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Test;
import scala.Console;
import scala.Option;
import cats.effect.IO ;
import cats.effect.Resource;
import es.weso.shex.validator.NoAction ;

import es.weso.shex.validator.ExternalResolver;

public class FhirTest {

  @Test
  public void testShex() {
    Console.withOut(OutputStream.nullOutputStream(), () -> {
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
        + "<http://hl7.org/fhirpath/System.String> .\n" // if this line is '<http://hl7.org/fhirpath/System.String> xsd:string', things work
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
        + "<http://hl7.org/fhir/Account/example> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://hl7.org/fhir/Account> ."
        + "<http://hl7.org/fhir/Account/example> <http://hl7.org/fhir/Account.status> _:b14 ."
        + "_:b14 <http://hl7.org/fhir/value> \"active\" .";

    Model model = Rio.parse(new StringReader(rdf), "", RDFFormat.N3);
    RDFAsRDF4jModel rdfmodel = new RDFAsRDF4jModel(model, Option.empty(), Option.empty());

    IO<ResultShapeMap> io = 
      Schema.fromString(shex, "ShexC", Option.empty(), Option.empty()).flatMap(schema -> 
      ResolvedSchema.resolve(schema, Option.empty()).flatMap(resolvedSchema -> 
      validate(resolvedSchema, rdfmodel)).flatMap(result -> 
      result.toResultShapeMap())); 
    ResultShapeMap map = io.unsafeRunSync();

    System.out.println("================================");
    map.associations().toList().toStream().foreach(assoc -> {
        System.out.println(assoc.info());
        return null;
      });
    System.out.println(map);

  }

 private IO<Result> validate(ResolvedSchema resolvedSchema, RDFAsRDF4jModel rdfmodel) {
    ExternalResolver noAction = NoAction.instance(); 
    Validator validator = new Validator(resolvedSchema, noAction, rdfmodel);
    IO<Result> result = validator.validateNodeShape(rdfmodel, IRI.apply("http://hl7.org/fhir/Account/example"), "http://hl7.org/fhir/shape/Account",false);
    return result;
 }

}