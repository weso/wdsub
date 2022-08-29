package es.weso.wdsub

import munit._
import es.weso.wbmodel.serializer.RDFSerializer._
import es.weso.wbmodel.serializer.RDFSerializer

class RDFSerializerTest extends FunSuite {

  test("Remove prefixes") {

    val str      = """|@prefix sh: <http://example.org/>
                 |@prefix : <http://pepe.org/>
                 |@prefix foo: <http://foo.org/>
                 |
                 |:x a :T .
                 |""".stripMargin
    val expected = """| :x a :T .
                      |""".stripMargin
    assertNoDiff(RDFSerializer.removePrefixes(str), expected)
  }
}
