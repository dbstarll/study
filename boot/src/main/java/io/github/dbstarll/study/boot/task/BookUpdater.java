package io.github.dbstarll.study.boot.task;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.study.entity.Book;
import io.github.dbstarll.study.service.BookService;
import io.github.dbstarll.study.service.UnitWordService;
import io.github.dbstarll.study.utils.CountBlock;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
class BookUpdater implements InitializingBean {
    @Autowired
    UnitWordService unitWordService;

    @Autowired
    BookService bookService;

    @Override
    public void afterPropertiesSet() throws Exception {
        updateWordCount();
    }

    private void updateWordCount() {
        for (Book book : bookService.find(Filters.gt("wordCount", 0))) {
            final CountBlock<ObjectId> count = new CountBlock<>();
            unitWordService.distinctWordId(unitWordService.filterByBookId(book.getId())).forEach(count);
            System.err.println(book.getId() + "\t" + book.getWordCount() + "\t" + count.getCount());

            if (book.getWordCount() != count.getCount()) {
                book.setWordCount(count.getCount());
                bookService.save(book, (Validate) null);
            }
        }
    }
}
