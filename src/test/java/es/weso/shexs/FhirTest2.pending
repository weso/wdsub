package es.weso.shexs;

import cats.effect.IO;
import cats.effect.Resource;
import cats.effect.Bracket;
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
import es.weso.rdf.jena.RDFAsJenaModel$;

public class FhirTest2 {

  @Test
  public void testShex() {
 /*   Console.withOut(
        OutputStream.nullOutputStream(), 
        () -> { */
            try {
        run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

/*      return null;
    }); */
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

    Model model = ModelFactory.createDefaultModel();

    Map<Quad, Long> lineNumbers = Maps.newHashMap();

    ParserProfile profile =
        new ParserProfileStd(RiotLib.factoryRDF(),
            ErrorHandlerFactory.errorHandlerStd,
            IRIResolver.create(),
            PrefixMapFactory.create(),
            RIOT.getContext(), false, false) {

          @Override
          public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line,
              long col) {
            Quad quad = super.createQuad(graph, subject, predicate, object, line, col);

            if (lineNumbers.containsKey(line)) {
              throw new IllegalStateException("Multiple triples on same line");
            }

            lineNumbers.put(quad, line);

            return quad;
          }

        };

    @SuppressWarnings("deprecation")
    ReaderRIOT parser = RDFParserRegistry.getFactory(Lang.NQ).create(Lang.NQ, profile);
    parser.read(new StringReader(rdf), null, null,
        StreamRDFLib.graph(model.getGraph()), RIOT.getContext());

/*    IO<RDFAsJenaModel> rdfmodel = RDFAsJenaModel.fromModel(model, Option.empty(), Option.empty(),
        new HashMap<>(), new HashMap<>()); */
    Option<IRI> base = Option.empty();    
    IO<Resource<IO,RDFAsJenaModel>> ioResourceRdf = RDFAsJenaModel$.MODULE$.fromString(rdf, "TURTLE", base, true); 

    Bracket<IO,Throwable> x = null;
    IO<Schema> io =
        ioResourceRdf.flatMap(resource -> 
         resource.use(rdfmodel -> getSchema(shex),x)); 

/*    IO<ResultShapeMap> io =
        ioResourceRdf.flatMap(resource -> 
         resource.use(rdfmodel -> 
         Schema.fromString(shex, "ShexC", Option.empty(), Option.empty()).flatMap(schema ->
            ResolvedSchema.resolve(schema, Option.empty()).flatMap(resolvedSchema ->
                validate(resolvedSchema, rdfmodel)).flatMap(result ->
                result.toResultShapeMap())))); */


    // ResultShapeMap map = io.unsafeRunSync();

    System.out.println("RDF + line numbers ================================");
    System.out.println(lineNumbers);

    System.out.println("Errors ================================");
/*    map.associations().toList().toStream().foreach(assoc -> {
      System.out.println(assoc.info());

      // is there a way to map these to the above 'lineNumbers' map?
      // somehow I'd like to produce an error message like: "active999999999999 on line 4 of the RDF is invalid"
      return null;
    });
    System.out.println(map); */

  }

  private IO<Result> validate(ResolvedSchema resolvedSchema, RDFAsJenaModel rdfmodel) {
    ExternalResolver noAction = NoAction.instance();
    Validator validator = new Validator(resolvedSchema, noAction, rdfmodel);
    IO<Result> result = validator.validateNodeShape(rdfmodel, IRI.apply("http://hl7.org/fhir/Account/example"), "http://hl7.org/fhir/shape/Account", true);
    return result;
  }

  private IO<Schema> getSchema(String schemaStr) {
    return Schema.fromString(schemaStr, "ShexC", Option.empty(), Option.empty());     
  }

  private void test() {
    Resource<IO,BufferedReader> res = /* Scala method that returns a resource */ null ;  
    // I asked this question here: https://gitter.im/typelevel/cats-effect?at=601e4cc4428d9727dd5bc4af
    // res.use(reader -> IO.apply(System.out.println("Reader...")));
    return ;
  }




}