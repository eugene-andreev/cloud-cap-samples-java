namespace my.bookshop;

using {
    Currency,
    managed,
    cuid
} from '@sap/cds/common';

entity Books : cuid, managed {
    title        : String(111);
    descr        : String(1111);
    author       : Association to Authors;
    stock        : Integer;
    price        : Decimal(9, 2);
    currency     : Currency;
    rating       : Decimal(2, 1);
    reviews      : Association to many Reviews
                       on reviews.book = $self;
}

entity Authors : cuid, managed {
    name         : String(111);
    books        : Association to many Books
                       on books.author = $self;
}

entity Reviews : cuid, managed {
    @cds.odata.ValueList
    book     : Association to Books;
    rating   : Rating;
    title    : String(111);
    text     : String(1111);
}

type Rating : Integer enum {
    Best  = 5;
    Good  = 4;
    Avg   = 3;
    Poor  = 2;
    Worst = 1;
}