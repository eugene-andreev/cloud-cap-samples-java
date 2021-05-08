using {sap.common.Languages as CommonLanguages} from '@sap/cds/common';
using {my.bookshop as my} from '../db/index';

@path : 'admin'
service AdminService @(requires : 'admin') {
  entity Books   as projection on my.Books actions {
    action addToOrder(order_ID : UUID, amount : Integer) returns Orders;
  }

  entity Authors as projection on my.Authors;
  entity Orders  as select from my.Orders;
}

// INSERT POC >>>>
// PoC for derfault
service AggregationService {
  @Aggregation.ApplySupported.PropertyRestrictions: true
    @Aggregation.CustomAggregate#totalStock : 'Edm.Decimal' 
    @Aggregation.CustomAggregate#averagePrice : 'Edm.Decimal'
    @Aggregation.CustomAggregate#averageRating : 'Edm.Decimal'
    @Aggregation.CustomAggregate#totalNicePrice : 'Edm.Decimal' 
    entity Books @readonly as projection on my.Books  {
    genre.ID as genreID,
    stock,
    price,
    rating,

    // Aggregate total stock value
  	@Aggregation.default: #SUM
    stock as totalStock, 
  
    // Aggregate average price 
  	@Aggregation.default: #AVERAGE // AVG works for CDS watch but not for JAVA  
    @Semantics.price.currencyCode: 'currency'
    price as averagePrice  : Decimal,
    @Semantics.currencyCode
    currency, 
  	
    // Aggregate for best rating
    @Aggregation.default: #MAX 
    rating as bestRating,

    // Aggregate for worst rating
    @Aggregation.default: #MIN   // 
    rating as worstRating,

    // Aggregate for average rating
    @Aggregation.default: #AVERAGE   // AVG works for CDS watch but not for JAVA  
    rating as averageRating : Decimal,

    // Aggregate with condition for total number of nice prices books < 13 bucks
  	@Aggregation.default: #SUM
    CASE WHEN price < 14 THEN 1 ELSE 0 END as totalNicePrice : Decimal,

  } excluding {ID, title};  

 
  /*entity Authors @readonly as projection on my.Authors  {
    name,
    dateOfBirth,
    dateOfDeath,

    @Aggregation.default: #AVERAGE  
    DAYS_BETWEEN(dateOfBirth, dateOfDeath) AS age : Decimal,
    @Aggregation.default: #MAX
    DAYS_BETWEEN(dateOfBirth, dateOfDeath) AS maxAge : Decimal,
    @Aggregation.default: #MIN   
    DAYS_BETWEEN(dateOfBirth, dateOfDeath) AS minAge : Decimal

     
  } 
  excluding { ID, books } */
}
// << END INSERT


// Deep Search Items
annotate AdminService.Orders with @cds.search : {
  OrderNo,
  Items
};

annotate AdminService.OrderItems with @cds.search : {book};

annotate AdminService.Books with @cds.search : {
  descr,
  title
};

// Enable Fiori Draft for Orders
annotate AdminService.Orders with @odata.draft.enabled;
annotate AdminService.Books with @odata.draft.enabled;

// workaround to enable the value help for languages
// Necessary because auto exposure is currently not working
// for if Languages is only referenced by the generated
// _texts table
extend service AdminService with {
  entity Languages as projection on CommonLanguages;
}
