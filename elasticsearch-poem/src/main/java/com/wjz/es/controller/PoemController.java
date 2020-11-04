package com.wjz.es.controller;

import com.wjz.es.domain.Poem;
import com.wjz.es.domain.PoemRepository;
import com.wjz.es.mapper.PoemMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/poem")
public class PoemController {

    @Autowired
    private PoemMapper poemMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private PoemRepository poemRepository;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("poems", poemMapper.list());
        return "/poem/list";
    }

    @GetMapping("/save")
    public String save() {
        Poem poem = new Poem();
        poem.setId("1");
        poem.setName("静夜思");
        poem.setAuthor("李白");
        poem.setCategory("唐诗");
        poem.setCategoryId("1");
        poem.setContent("静夜思 唐 李白 床前明月光，疑是地上霜。举头望明月，低头思故乡。");
        poem.setAuthorDesc("李白（701年－762年），字太白，号青莲居士，又号“谪仙人”，唐代伟大的浪漫主义诗人，被后人誉为“诗仙”，与杜甫并称为“李杜”，为了与另两位诗人李商隐与杜牧即“小李杜”区别，杜甫与李白又合称“大李杜”。");
        poemRepository.save(poem);
        poemMapper.save(poem);
        return "forward:/poem/list";
    }

    @GetMapping("/update")
    public String update() {
        Poem poem = new Poem();
        poem.setId("1");
        poem.setAuthorDesc("大诗人，李白（701年－762年），字太白，号青莲居士，又号“谪仙人”，唐代伟大的浪漫主义诗人，被后人誉为“诗仙”，与杜甫并称为“李杜”，为了与另两位诗人李商隐与杜牧即“小李杜”区别，杜甫与李白又合称“大李杜”。");
        poemRepository.save(poem);
        poemMapper.update(poem);
        return "forward:/poem/list";
    }

    @GetMapping("/clear")
    public String clear() {
        poemRepository.deleteAll();
        return "forward:/poem/list";
    }

    @GetMapping("/rebuild")
    public String rebuild() {
        poemRepository.deleteAll();
        poemRepository.saveAll(poemMapper.list());
        return "forward:/poem/list";
    }

}
