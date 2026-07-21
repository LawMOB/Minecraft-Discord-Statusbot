package mopsy.productions.discord.statusbot;

import java.util.Base64;

public final class DefaultServerIcon {
    private DefaultServerIcon() {}

    private static final String BASE64_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAASIElEQVR4nO1a2Y9U1bf+zjxU19SDNC0yhiYiwYSYGCVEH9X44pOJ" +
            "f4b/zO/dJ140xkQxkBh9QYYQUWhteqAn6KG6i6qu6czn/B6433JXAfcqyU9uLncnla4+dc7ea6/h+9Za+2hff/11AQBFUeBlGpqm" +
            "oSgKmPyiadqLlukfH5qmQX/RQrzo8f8KeNECvOihA3gp45/jpfYATdNeXhYQGnzRgvyToygKyXd0XYeu648VQG38Xx5FUcCyLJRK" +
            "JTiOA8uyYJrmy+MBRVHA930cPHgQWZbJtZdCAZqmQdM05Hn+BN6Zozc+Tyjwuf8NYMr1KQ9lKooCWZYhCAJEUYQkSZCm6ZMe8D9t" +
            "QFWQunHHceR6kiRy338KW55lLG5YYtw0Yds2bNuGaZpoNBpIkkT2aaoT/tWF1cGJFxYWHqOqaWJqagq2bQ8t9J8Y6txFUcC2bUxO" +
            "Toocuq4P3aPrOmq1Gh49evSnAp7X7YmqzWYTt27dwq1bt2SeCxcu4K233oJlWXLtn2AaTdPg+/5TZS2KAkmSwLIsGIaBLMseJ0J8" +
            "8HkWy7IM3377LRYWFuA4jlj9p59+QrfbxYcffiibzvN8SAnPWnM0xP7q4HNZlsknTVP5y+95ng/N/dwsoOs6giBAr9eDbdtDQpim" +
            "iSAIsL+/D8/zxD25OO99WhY6CmJ/NVPVdR1pmmJ7e3sIANU5R78/Nw2OIn6WZTAMA2mayu8rKyv44osvMDY2htnZWZw/fx66rsOy" +
            "LJmHChm1jBrDxJEsy4a841kAGEURbNuGrv9Z5pD++J1rDXWEnmdomoZqtYoHDx7I5nVdR6VSQVEU6PV6aDabKJVKAB6zw9raGvb3" +
            "99HpdLC3t4dTp07h9ddfh23bQ1R17949NJtNdLtdVCoVnD17FhMTE0jTVHj9WbJTaaoXqN5E2QE8XyrM+23bxqeffoorV67g6tWr" +
            "4gmu60rcaZqG1dVVfPfdd8iyDKurq/J8nufY2trC+vo6PM/D1NQUzp49i7W1NVy+fBn9fh+O46BUKqFcLuPIkSMwDEM8BABarRbi" +
            "OB7ag7pxVWb1L78/VwjQTTVNQxzH+OCDD/DKK6/g4sWLOHXqFJrNJjqdjggZBAF+//13eJ4H27ZhGAYsy0KWZRgMBtje3oZhGFhb" +
            "W0Oj0UAURUjTVHAjiiJcvXoVvu/jo48+EgTnZrvdLoIgeKYhn4Ujz10OMybn5+dx584dlMtlsf7+/j7a7TbSNEWSJMjzHK7rol6v" +
            "oygKxHEscZjnORzHEQF1XcfPP/+MTqcD27ZlHVr8zp07OHfuHGq1GpIkQaPRwNjY2FBIqDJSQaoS1CEg+Dx0c/nyZfzxxx8olUrY" +
            "2NhAu92GYRjY3NyE67pi4QMHDqBUKkkBomkaHMdBURRI0xSGYSAIApimiTzPkWUZHMcRTAEgoXDkyBEkSYJWq4UrV67g7t27mJiY" +
            "wAcffICJiQnEcfyEnFQMy19d12EYhnz+dggQmeni9J5yuSxW13UdpVIJtm3DdV1BeS5Ka7iuK5Y3DANJkmB6ehobGxuIokjuJXvM" +
            "zs7Ctm0sLCzg5s2bKIoCzWYTnufh448/hud50DRN1uG86uZHPeVvs0BRFLLxUqkETdNkk3EcwzRNOI4Dx3FgmiaSJEEcx9B1HVmW" +
            "wbKsx0WIaQ7RJ2sJ27Zx4MABdDoddDodnDhxArZt48KFCzh27Bh++OEHXLt2bSiEbt68icnJSXz22WeIougJrueHhiAA53kO86/G" +
            "v0ojruvi0KFD+OWXXzA+Pi5p5vj4OPr9PnRdRxzHCMNQLMg5wjCEruvwfX9oPippMBhgbGwM/X4fSZKgVCrBNE08fPgQb7/9Nmzb" +
            "RqvVkjnzPEccx9jc3EQURQiC4ImNj37UPemu68IwjKGHnjYorGVZ2N3dxcOHD+H7PrrdrrhoFEUwTXOIq3VdF43T3fM8R7/flzXT" +
            "NJV7yCzVahW1Wg1hGAIAlpaWcPHiRdTrdZw+fVo8p16v45133sG5c+fQ6/UQRRHiOJZyV02CnoYF2rVr1wqCD2N4NOtSh2EY+Oqr" +
            "r9BsNmEYBjqdDizLkoYDAFEAN6umwsQIehTDh6ksAdC2bQG/arWKPM/FO86fP49KpYJ//etfePfdd/HJJ58gDEMB2r8zTFrdMAyJ" +
            "3263K+6lWl/XdYRhiEePHslGXddFFEVyHwBxcV3X0e/3YVmWFEpUAgcTJtd1hfPpTQRGKsswDAwGA1y6dAmVSgWdTkfmSJJE8o7R" +
            "8d9hnD43Nyc30R05qWmaQ7m7ZVloNBrodrtCWyyEVA9gLs74VsOC/QPVQ3zfl9yfCmF2B0C6OARO3/cRxzEmJyfFE6lw0zT/pLj/" +
            "+q66/ROf1dVVPHz4UDbQ7XZx9+5dXL9+HYuLi9jb2xOr8sNBaiOgsQtjWZZwMhmDFESK5P9Jkkjc8lld14eUyMyRtGgYBuI4xvT0" +
            "NLa3t/Hll1+i3+8LLhDh+f1ZXlEUBcwsy7C2toZqtQrLsrC3t4eNjQ04joPl5WWsrKzgyJEjOHnyJABgYWFB6m0CVKlUegJIkyRB" +
            "kiTwPE8okBbN81woNEkSDAYDGIYBx3FEYCojiiJx4TiOh3INJk1zc3OIogjvvfceqtUqoigaMpQ6RvMEnW51+/Zt9Ho9oSlaVNd1" +
            "tFot2LaN+fl53L17F/V6HeVyGZVKBWmaYjAYDKWaQRBIAyIMwyF2yfMcjx49QhAEiONY8n7+rnpGHMcolUpwXVc2xDxCtbDneVhf" +
            "X8f333+Pra0t+L4vqTQ3rX7vdDpYWFjAjRs3YNZqNXHBbreLRqMh2iX4RFGE3377DdevXxcA63a7CMNwaBP0AKKxCqIUIAxDJEmC" +
            "ZrMp6M+DiiiKJE12XRf7+/uoVCpwHEeUTNzgUP8PggA//vgjzpw5A9d1cfjwYfi+P5QcWZaFlZUVXLp0CY1GA+a9e/cwPT0Nz/Pw" +
            "4MGDoUxPTUdv376NVqslOT47PkR227ZRLpfFUrRar9cTulM5n0CnntIQ+JIkgeu6MoeaDkdRJHyvgl0QBNjb24Nt21haWkKlUsHR" +
            "o0fx5ptv4vjx48IShmFgaWlJ+hTm3NwcsizD4cOHpYnJicMwhGVZaLVa2Nvbk9+J1nEcD1EMcYDD8zzJKzRNw2AwEG8jgMZxLAA2" +
            "qvw4juF5nngTabXZbCIMQ2l5U3n0Et/3MTExgVu3bqFSqWB2dhZFUaDdbuPixYtYXl4WbzTTNMXc3Bzu37+Po0ePYmZmRhqcURQh" +
            "DEMEQSCKyfMcjUYDQRAIGFKATqcjlSBBkFyeJAkePXqEXq8noUVWYTixV+B5nrg9AZFM4LruEJJTwUylqehOp4ODBw8ONVAI+DRW" +
            "mqbQKVwQBIL6YRg+/lHXcebMGUxPT4vV2MRgzKt9QXXTbHGRXoMgQLlclt+pGJbHmqZJvkDrj42NDRVUjUYD+/v7UtSw5oiiSGQj" +
            "Huzs7GB7exs7OzvodrtwXRdhGErFyOclE2QWtbu7C9M08cYbb4jLs9NKrZOfmW+rLS42PXzfR6/Xg+/7AkS+72NsbAzdbleUwzBS" +
            "8wxaK01TeJ6HdruNxcVF9Ho9UR4LK26Y1zgvPe7Bgwf45ptvEEURdnZ20Gw2ZZ2iKGDGcQzDMIQ7ybWu68LzPKysrKDZbAo6O46D" +
            "6elpRFGEfr+PdrstyM/CqlQqSV7PWE+SBI7jSIFDCmNJzHvK5bKEVJ7n2N/fR1E8PtkdDAYCwirmDAYDKb+pBDJZt9vF9vY2lpeX" +
            "4TjOUBWZpilMoqhauQ0GA9y7dw9jY2PSrGy32+LmpDUKkKYpSqUSPM8T5FYTH8ZxmqZotVriNcQPJj2lUkk2SIoknqgtMlI0GUjT" +
            "tKHKjwWV4zjIsgz9fh9ZlqHX60HXddTrdeksmYxlxiaBhhuJ41ispPI8MzKVi7kx8i43TSBivkCaYz+P1uT9VBg3RkUdPXpUPGN9" +
            "fR2apuHw4cOwbRuWZWFpaQlhGOLw4cOoVCrI8xytVgtbW1tDGeru7q6sZ6qapmXpyuROarlcLgsIBUEgNKn272gdsgYPKljplctl" +
            "EdiyLKEzKoRrMYOkR7A2YNhwDZUubdsWllG7VKxLmNMQ4AHArFQqyLIMlUoFvu8jz3NYloU8z1GpVKDrutBbp9NBURQIgkCSH1JP" +
            "qVQa6rqw6cHnVUvati0FFI/OWH06jjPULKECRjfPDfZ6PaRpKgBJft/d3ZUSmyHJsKRhTNN8rAAi52AwEFdlwQJAhCX48LydQjKO" +
            "R62v67qgLimIAtEKLIejKMLe3t5Q9agmSwRI5hJUOLNAHnkXxeMTqf39fdRqNZTLZbiui93dXTHc1NQUXn31VQwGgz/b4qNlq2VZ" +
            "aLfbACAFSblcRrfblYWI3kxC2Bnu9XpwHAeu6woH1+t19Ho9wZgwDMVijEe1H+E4jnB8tVoVb/J9H6VSSRQZhiFqtRpc15WkiJ5G" +
            "T7QsC/V6fQgcLct6nGeQ3wlWajHDCYiwlmXB8zzZiNpdZR5P7CCfsxs0NjYmmR15mugehuFQV4nASJSvVqtDFMgU2zAMVKtVxHGM" +
            "mZkZKbDoSQDQbrdRLpcxNjYmzZlut4vFxUXUajWY586dA4Chzg+tcuLECTx8+FBc57XXXkOSJNja2sLJkyexubmJPM8xPT2N+/fv" +
            "4+DBgwCAzc1NzM7OQtd1zM/Po1arYXp6GsvLyyiKAtVqFVtbWzhw4ICcIgEQuqUyx8fH5chramoKGxsbKJfLAr6bm5tYXl5GuVxG" +
            "o9HAsWPHsLOzg8nJSRw6dEg61SzSCLjqibM5OTkpMT9aP3ueJ9pWQc+yLPi+D8/zEEWR0CZTWj5HcHQcBxMTE7JZ8vDU1BR835e6" +
            "ol6vC9jmeY5arSZhUqvV0G63hdvVt09IrfRAy7IwPj6ObrcrnSSyme/78qZIFEUwgyAYajzyKGtpaUlecGg2m9A0DTs7Ozh06BAA" +
            "4MGDB6jValhaWkK/38fk5CTW19cxOzsLy7KwvLyM06dPY2ZmBouLi3AcB/Pz81heXsbx48cxMzMjTRG2t1ZXVzE1NSUeyR7FwYMH" +
            "cf/+fdRqNVy9ehW7u7solUro9/uoVqtot9sYHx/H1tYWer0eZmZmMDU1hcFggBs3bsihC7GBRs6yDKZt20JFQRDAdV3pqBDp+ZcA" +
            "5nmexLeaXtI7Wq0Wfv31V+zu7mJ2dlbabez/dTodTE5OSo+AXShmk7ZtS/JFKg7DcCgbZTLGlhmBtygK7O/vY21tDc1mc4himdgR" +
            "7AFA+/zzzwu609bWlgjCA496vY7BYCA9ejUXHwwGUqr2+30URQHP89DtduXFhnq9Ltkk6412uw3XdYc2xwKKnePBYIDBYCDK397e" +
            "Fh6ncviX55W8d7TFRosDw10qwzCgvf/++wVjgxRCCzBl5XWeAfDIm8kNsYDIz9aWaZrS7ODiLJPJHIxP5hBcn2zC31utlmCTWuyo" +
            "x24cauWqXlN7k5IJVqvVoQm5CPN6tqvUtz+IGbQ+n1NLa25o9NyArkvMoTLUI7RROVhJjr7qSiORntWSWK36CPCcT/UEs9frDb2w" +
            "wCKE/E83Zy5g27ZYnptTT11V7apHY5x31EXp0qzR1XloObrraI+f+cTougCGvI7Pcy71wNZkPKtMoNLh0wSitbjoqMY5eABJi8qi" +
            "ijLU/1UvUj/MTvm7+iyLOFV+VXHqGG3F5XkOkwkQJydNqPFCbaquOaoQ9Zr6UiR5WRVI3SgFV/sHxJJRq6oKUBXC80bVq1QcoDw0" +
            "lto6M9m2ZqJAxFatqQrEHoD6SouqlNH/1b696jn0LLXXoMYqN6Leq5btnI9eNlpLcJ3RZg9xSdpv5EU1TsitarZFsFEbIlQKY1gF" +
            "NPWdIA5qngCpHlyOgic3rnofP5xX3RjnpUJHD1HUa8wJ8jyHqd6kgg2TDfXlYjXWVY2qh6ZUIsOCwqmuqVZqqgKBP2sScvsoFqjM" +
            "Q2Oorq+W6QRb1fMoA/uSpoquar1Pa9CC7OqMciyF4SYInKrlVRd/GripzUyykQpYPCtgH2GUz3kPj9ZJ4arl1W6ymg2aLHTUtzso" +
            "2GhGpS6koj0FUvvtXJSLcUFVICruaZvifUT4IAjkGu9hOq6GBXFCZQ5+uJZ6r6nG2KhlHceRSlDtE4xSo6phNi3pguqGeY2KohJV" +
            "ZFePrpl1qkkTlUNLRlEkcj0tnPmdfcFRNvo39M4KxJbbc+sAAAAASUVORK5CYII=";

    private static volatile byte[] cachedBytes = null;

    public static byte[] getBytes() {
        if (cachedBytes == null) {
            synchronized (DefaultServerIcon.class) {
                if (cachedBytes == null) {
                    cachedBytes = Base64.getDecoder().decode(BASE64_PNG);
                }
            }
        }
        return cachedBytes;
    }
}
