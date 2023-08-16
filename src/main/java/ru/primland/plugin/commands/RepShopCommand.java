package ru.primland.plugin.commands;

public class RepShopCommand /*implements CommandExecutor*/ {
    /*private Config config;

    public RepShopCommand(Config config) {
        this.config = config;
    }

    public List<Integer> processSlots(@NotNull Map<?, ?> item) {
        if(item.containsKey("slot"))
            return List.of((Integer) item.get("slot"));

        if(item.containsKey("slots")) {
            List<Integer> output = new ArrayList<>();
            Utils.convertObjectToList(item.get("slots")).forEach(rawSlots -> {
                if(rawSlots == null) return;
                String[] slots = rawSlots.toString().split("-");

                if(slots.length == 1) {
                    output.add(Integer.parseInt(slots[0]));
                    return;
                }

                for(int i = Integer.parseInt(slots[0]); i <= Integer.parseInt(slots[1]); i++)
                    output.add(i);
            });

            return output;
        }

        PrimPlugin.send("&cВы должны указать параметр slot или slots (rep_shop.yml)");
        return null;
    }

    public Map<?, ?> getCategoryById(String id) {
        for(Map<?, ?> category : config.getMapList("categories")) {
            if(!category.containsKey("id") || !String.valueOf(category.get("id")).equals(id)) continue;
            return category;
        }

        return null;
    }

    public ItemStack processMaterial(@NotNull String material, ItemStack item) {
        if(material.startsWith("[cc]box")) {
            String[] args = material.split(" ");
            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;

            ItemStack box = GiveBoxCommand.getBox(Config.load("modules/cards.yml"), amount);
            if(box == null) return null;

            item.setType(box.getType());
            item.setData(box.getData());
            item.setItemMeta(box.getItemMeta());
            item.setAmount(box.getAmount());
            return item;
        }

        if(material.startsWith("[cd]discs")) {

        }
    }

    public ItemStack processItem(Map<?, ?> item, boolean isCategoriesPage) {
        if(isCategoriesPage && !item.containsKey("material")) {
            PrimPlugin.send("&cПараметр material является обязательным для категорий (rep_shop.yml)");
            return null;
        }

        if(isCategoriesPage) {
            ItemStack output = new ItemStack(Material.valueOf(item.get("material").toString()));
            if(output.getType() == Material.PLAYER_HEAD && item.containsKey("texture"))
                output = NBTUtils.addTexture(output, item.get("texture").toString());

            ItemMeta meta = output.getItemMeta();
            if(meta == null) return null;
            meta.setDisplayName(config.getString("display.displayName"));

            List<String> lore = new ArrayList<>();
            config.getMapList("display.lore").forEach(line -> {
                String type = line.get("type").toString();
                String text = line.get("text").toString();
                List<?> show = line.containsKey("show") ? Utils.convertObjectToList(line.get("show"))
                        : List.of("product", "category");

                if(!show.contains("category"))
                    return;

                if(type.equals("line"))
                    lore.add(Utils.parse(text, new Placeholder("name", item.get("name").toString())));

                if(type.equals("template"))
                    Utils.convertObjectToList(item.get("description")).forEach(oLine ->
                        lore.add(Utils.parse(text, new Placeholder("name", item.get("name").toString()),
                                new Placeholder("original", oLine))));
            });

            meta.setLore(lore);
            output.setItemMeta(meta);
            return output;
        }

        Map<?, ?> category = getCategoryById(String.valueOf(item.getOrDefault("category", null)));
        if(category == null) {
            PrimPlugin.send("&cВы должны указать категорию товара (rep_shop.yml)");
            return null;
        }

        if(!category.containsKey("items")) {
            PrimPlugin.send("&cУ категории нет настроек для товаров (rep_shop.yml)");
            return null;
        }

        Map<?, ?> itemConfig = Utils.convertObjectToMap(category.get("items"));
        ItemStack output = new ItemStack(Material.valueOf(itemConfig.get("material").toString()));
        if(output.getType() == Material.PLAYER_HEAD && item.containsKey("texture"))
            output = NBTUtils.addTexture(output, item.get("texture").toString());


    }

    public CustomMenu setupPage(int page, int pages, List<Map<?, ?>> items, boolean isCategoriesPage) {
        CustomMenu menu = postprocessPage(new CustomMenu("", 9), page, pages);

        config.getMapList("items").forEach(mItem -> {
            if(!mItem.containsKey("type") || mItem.get("type") == null) {
                PrimPlugin.send("&cПараметр type является обязательным (rep_shop.yml)");
                return;
            }

            String type = mItem.get("type").toString();
            List<Integer> slots = processSlots(mItem);
            if(type.equals("_item")) {
                AtomicInteger index = new AtomicInteger();
                items.forEach(rawItem -> {
                    ItemStack item = new ItemStack();
                    menu.setItem(slots.get(index.get()), );
                    index.getAndIncrement();
                });
            }
        });
    }

    public CustomMenu postprocessPage(@NotNull CustomMenu page, int pageNumber, int pages) {
        EpsilonEngine engine = new EpsilonEngine();
        engine.addPlaceholders(new Placeholder("page", pageNumber), new Placeholder("pages", pages));

        page.setTitle(engine.process(config.getString("title")));
        page.setSize(config.getInteger("size", 54));

        engine.clearPlaceholders();
        return page;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        CustomMenu menu = new CustomMenu(config.getString("title"), config.getInteger("size", 54));

    }

    public void updateConfig(Config config) {
        this.config = config;
    }*/
}
