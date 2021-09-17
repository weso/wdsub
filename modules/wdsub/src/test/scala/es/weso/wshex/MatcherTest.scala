package es.weso.wshex
import munit._

class MatcherTest extends FunSuite {

  test("Q42_simple") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       |  :P31 [ :Q5 ]
                       |}""".stripMargin
    val eitherMatcher = Matcher.unsafeFromString(schemaStr) 
    eitherMatcher.fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(matchStatus.matches, s"Doesn't match Q42\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple with an extra passes") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       |  :P31 [ :Q5 :Q6 ]
                       |}""".stripMargin
    val eitherMatcher = Matcher.unsafeFromString(schemaStr) 
    eitherMatcher.fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(matchStatus.matches, s"Doesn't match Q42\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple fails") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       |  :P31 [ :Q6 ]
                       |}""".stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(!matchStatus.matches, s"Match Q42 but should fail\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple OR passes") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       | :P31 [ :Q6 ]
                       |} OR {
                       | :P31 [ :Q5 ]
                       |} """.stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(matchStatus.matches, s"Match Q42 should validate with OR\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple OR fails if all fail") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       | :P31 [ :Q6 ]
                       |} OR {
                       | :P31 [ :Q7 ]
                       |} OR {
                       | :P31 [ :Q8 ]
                       |}""".stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(!matchStatus.matches, s"Match Q42 should fail with ORs failing\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple AND fails if one fails") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       | :P31 [ :Q5 ]
                       |} AND {
                       | :P31 [ :Q6 ]
                       |}""".stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(!matchStatus.matches, s"Match Q42 should fail with ANDs when one fails\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple AND passes if all pass") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> {
                       | :P31 [ :Q5 ]
                       |} AND {
                       | :P31 [ :Q5 ]
                       |}""".stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(matchStatus.matches, s"Match Q42 should pass with ANDs when all pass\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple NOT passes if internal fails") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> NOT {
                       | :P31 [ :Q6 ]
                       |} """.stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(matchStatus.matches, s"Match Q42 should pass with internal NOT fails\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q42_simple NOT fails if internal passes") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> NOT {
                       | :P31 [ :Q5 ]
                       |} """.stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(!matchStatus.matches, s"Match Q42 should fail when internal NOT passes\nMatchStatus=${matchStatus})")
      }
    )     
  }

  test("Q NOT fails if internal passes") {  
    val q42Str = """|{"type":"item","aliases":{},"labels":{},"descriptions":{},"sitelinks":{},"id":"Q42","claims":{"P31":[{"rank":"normal","references":[{"snaks":{"P214":[{"snaktype":"value","property":"P214","datavalue":{"type":"string","value":"113230702"}}],"P248":[{"snaktype":"value","property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"}],"P813":[{"snaktype":"value","property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}]},"allSnaks":[{"property":"P248","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":54919}},"datatype":"wikibase-item"},{"property":"P214","datavalue":{"type":"string","value":"113230702"}},{"property":"P813","datavalue":{"type":"time","value":{"time":"+00000002013-12-07T00:00:00Z","timezone":0,"before":0,"after":0,"precision":11,"calendarmodel":"http://www.wikidata.org/entity/Q1985727"}},"datatype":"time"}],"snaks-order":["P248","P214","P813"]}],"mainsnak":{"snaktype":"value","property":"P31","datavalue":{"type":"wikibase-entityid","value":{"entity-type":"item","numeric-id":5}},"datatype":"wikibase-item"},"id":"Q42$F078E5B3-F9A8-480E-B7AC-D97778CBBEF9","type":"statement"}]}}""".stripMargin
    val schemaStr = """|prefix : <http://www.wikidata.org/entity/>
                       |
                       |start = @<Human>
                       |
                       |<Human> NOT {
                       | :P31 [ :Q5 ]
                       |} """.stripMargin
    Matcher.unsafeFromString(schemaStr).fold(
      parseError => fail(s"Error parsing schema: $parseError"),
      matcher => {
        val matchStatus = matcher.matchJsonStart(q42Str)  
        assert(!matchStatus.matches, s"Match Q42 should fail when internal NOT passes\nMatchStatus=${matchStatus})")
      }
    )     
  
}
}