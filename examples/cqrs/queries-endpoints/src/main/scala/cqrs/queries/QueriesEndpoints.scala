package cqrs.queries

import java.util.UUID

import endpoints.algebra.{CirceEntities, Endpoints, MuxRequest}
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait QueriesEndpoints extends Endpoints with CirceEntities {

  /**
    * This is our *internal* protocol for queries. We don’t have to suffer from
    * REST conventions:
    *  - our client doesn’t care about the “semantic” difference between POST and GET.
    *  - status codes other than 500 and 200 are useless: the query is built
    *    via a statically typed API, so we can not build bad requests, by construction, and the response
    *    entity gives way more details about failures than status codes.
    */
  val query: MuxEndpoint[QueryReq, QueryResp, Json] =
    muxEndpoint[QueryReq, QueryResp, Json](post[Unit, Json, Unit, Json](path / "query", jsonRequest), jsonResponse)

}

/** A request carrying a query */
sealed trait QueryReq extends MuxRequest
final case class FindById(id: UUID, after: Option[Long]) extends QueryReq { type Response = MaybeResource }
final case object FindAll extends QueryReq { type Response = ResourceList }
// TODO Add a type of query including complex filters

object QueryReq {
  implicit val queryDecoder: Decoder[QueryReq] = deriveDecoder
  implicit val queryEncoder: Encoder[QueryReq] = deriveEncoder
}

/** A response to a QueryReq */
// TODO Enrich with failure information
sealed trait QueryResp
case class MaybeResource(value: Option[Meter]) extends QueryResp
case class ResourceList(value: List[Meter]) extends QueryResp

object QueryResp {
  implicit val queryDecoder: Decoder[QueryResp] = deriveDecoder
  implicit val queryEncoder: Encoder[QueryResp] = deriveEncoder
}
