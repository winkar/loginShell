#!/usr/bin/env ruby

def capabilities
    {
        'browserName' => '',
        'device' => 'android',
        'version' => '4.1',
        'app-activity'=> 'com.taobao.tao.Welcome',
        'app-package'=> 'com.taobao.taobao'
        #'app-activity'=> '.activity.MainActivity',
        #'app-package'=> 'com.sankuai.meituan'
    }
end

def setup
    server_url = 'http://127.0.0.1:4723/wd/hub'
    @driver = Selenium::WebDriver.for(:remote, :desired_capabilities => capabilities, :url => server_url)
    @driver.manage.timeouts.implicit_wait = 30 # seconds
    puts 'sleep 20'
    sleep 20
    @driver
end

def config()
    @config={}
    @config['blacklist']=['立即抢购', '美团承诺 团购无忧', '退款']
end

def refresh()
    root=[]
    el_array=@driver.find_elements(:xpath, "//text[@clickable=true]")
    #如果点击没有发生变化, 就跳过
    if el_array.size==@list.size
        return @list
    end
    el_array.each do |node|
        begin
            text=node.text
            location=node.location
            is_skip=false
            next if text.strip==''
            next if text.size<2
            @config['blacklist'].each do |keyword|
                if text.index(keyword)
                    is_skip=true
                    break
                end
            end
            next if is_skip
            current={}
            current['click']=false
            #位置相差不大 也认为是相同
            current['sign']=text+'|'+location.x.to_s[0..1]
            current['text']=text
            current['object']=node
            root << current
        rescue Exception=>e
            puts e.message
        end
    end
    root
end
def find_return_root(el)
    current=nil
    @nodes.each do |node|
        if node.content['sign']==el['sign']
            current=node
            break
        end
    end
    current=current.parent if current
    return(current)
end
def travel()
    @list||=[]
    @nodes ||= Tree::TreeNode.new("ROOT", {})
    @current||=@nodes
    @index||=0
    @index+=1
    @list=refresh()
    has_new=false
    return_root=nil
    @list.each do |el|
        #判断是否曾经出现过
        return_root=find_return_root(el)
        if return_root==nil
            has_new=true
            #如果是新元素, 就添加到tree中
            @current << Tree::TreeNode.new(el['sign'], el)
        end
    end
    #如果没有新元素, 代表回到某个父节点
    if has_new==false
        @current=return_root
    end
    save()
    if @current.level>7
        @driver.navigate.back
        travel()
    end

    #从未被点击的地方点击
    current=nil
    @current.children.each do |child|
        if child.content['click']==false
            current=child 
            @list.each do |node|
                if node['sign']==current.content['sign']
                    current.content['click']=true
                    @current=current
                    node['object'].click
                    sleep 3
                    travel()
                    break
                end
            end
            break             
        end
    end
    if current==nil
        @driver.navigate.back
        travel()
    end
end
