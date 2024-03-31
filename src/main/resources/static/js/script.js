const toggleSlidebar = ()=>{
    if($(".slidebar").is(":visible")){
        $(".slidebar").css("display","none");
        $(".content").css("margin-left","20px");
    }
    else{
        $(".slidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }

}

const search=()=>{
    const query = $("#search-input").val();

    if(query==''){
        $(".search-result").hide();
    }
    else{
       
        console.log(query);

        let url = `http://localhost:8080/search/${query}`

        fetch(url).then((res)=>res.json()).then((data)=>{
            console.log(data);
            let text = `<div class='list-group'>`;
            data.forEach(contact => {
                text+=`<a  href='/user/contact/${contact.cId}'  class='list-group-item list-group-action'> ${contact.name} </a>`
                
            });
            
            
            text+= `</div>`;

            $(".search-result").html(text);

            $(".search-result").show();



        });

    }



};