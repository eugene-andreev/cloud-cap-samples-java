using {my.bookshop as my} from '../db/books';

@path : 'catalog'
service CatalogService {
    entity Books as projection on my.Books;
}
