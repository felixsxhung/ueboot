/**
 * Created by yangkui on 2017/10/18.
 * 常用工具类，如将数组转换为树装结构的数据
 */

export default {
  /**
     * 获取树状数据
     * @param tree 原始数组数据，必须包含name,value,id,parentId 几个属性
     * @param handlerItem 回调方法，用于指明如何处理每个节点数据（如何拼装对象），如果未指定则用默认方式处理
     *  function handlerItem(item){
     *      let object = {}
     *      object.text = item.name
     *      object.value = item.id
     *      return object
     *  }
     *
     * @returns {Array} 树状结构的数据，id,label,value,parentId,attr,children
     */

  getTreeData (tree) {
    // 建立索引，便于后续组装树数据
    let treeObject = {}
    /**
         * 1.先循环一次，根据parentId先归类
         * 2.对已经归类的parentId进行逐级填充找到下级，递归找到最下级。
         * 3.同时从归类里面删除已经归并到父级的数据
         */
    let roots = this.getParentTreeData(tree, treeObject)
    let keys = Object.keys(roots)
    keys.forEach((key) => {
      let root = roots[key]
      if (root && root.children.length > 0) {
        let object = treeObject[root.id + '']
        root.opened = this.getChild(roots, root, object ? object.name : '', treeObject)
      }
    })
    keys = Object.keys(roots)
    let array = []
    keys.forEach((key) => {
      roots[key].children.forEach((c) => {
        array.push(c)
      })
    })
    return array
  },
  // 获取根据parentId归类的树数据
  getParentTreeData (tree, treeObject) {
    let roots = {}
    for (let i = 0; i < tree.length; i++) {
      let item = tree[i]
      if (treeObject) {
        treeObject[item.id + ''] = item
      }
      let parentId = (item.parentId || 'root') + ''
      item = this.assembleItem(item, null, null)
      let root = roots[parentId]
      if (root == null) {
        root = { id: parentId, children: [item] }
      } else {
        root.children.push(item)
      }
      roots[parentId] = root
    }
    return roots
  },
  getChild (roots, item, parentPath, treeObject) {
    let children = []

    // 判断子节点是否有勾选的，且不是全部勾选
    let undetermined = false
    let allSelected = true
    for (let i = 0; i < item.children.length; i++) {
      let o = item.children[i]
      let child = roots[o.id + '']
      // 拼装对象
      let b = this.assembleItem(o, parentPath, null)
      // 存在子节点，则递归查找子节点
      if (child && child.children.length > 0) {
        this.getChild(roots, child, b.path ? b.path : b.name, treeObject)
        b.children = child.children
        b.undetermined = child.undetermined
        b.selected = child.selected
        b.opened = child.selected
        if (child.selected || child.undetermined) {
          b.opened = true
        }
      } else {
        // 保留当前节点的子节点
        b.children = o.children
      }
      // 子节点存在半选、选中时，父级节点都为半选
      if (b.undetermined) {
        undetermined = true
      }
      if (b.selected || b.opened) {
        undetermined = true
      } else {
        allSelected = false
      }
      children.push(b)
      // 删除已经填充到parent下的数据
      if (child) {
        delete roots[b.id + '']
      }
    }
    item.children = children
    item.undetermined = undetermined
    item.selected = allSelected
  },

  getTreeData2 (tree, handlerItem) {
    // 构造树结构
    let roots = []
    // 临时保存所有根节点数据，用于快速判断使用
    let rootTree = {}
    // 1.查找每个根节点。或者查找每个父级节点不存在的节点当根节点
    for (let i = 0; i < tree.length; i++) {
      let item = tree[i]
      let isRoot = true
      // 如果当前节点的父节点已经存在，则直接跳过，
      if (rootTree[item.parentId + '']) {
        continue
      }
      // 查找一下当前节点所在的父亲节点是否存在，如果不存则当根节点
      for (let i = 0; i < tree.length; i++) {
        if (item.parentId === tree[i].id) {
          isRoot = false
          break
        }
      }
      if (!isRoot) {
        continue
      }
      let root = this.assembleItem(item, null, handlerItem)
      let result = this.getChildren(tree, item.id, item.path)
      if (result['child'] && result['child'].length > 0) {
        root.children = result['child']
      }
      root.opened = result['hasSelected']
      roots.push(root)
      rootTree[item.parentId + ''] = true
    }
    // todo 是否需要排序
    return roots
  },
  // 组装树结构对象
  assembleItem (item, parentPath, handlerItem) {
    let o = {}
    if (handlerItem) {
      o = handlerItem(item)
    } else {
      if (parentPath) {
        o.path = parentPath + '\\' + item.name
      } else {
        o.path = item.name
      }

      // 搜索的时候，会产生label属性，显示的内容格式与name不一样
      o.text = item.label ? item.label : item.name
      // 防止多次assemble后，导致label不存在了
      o.label = o.text
      o.name = item.name
      o.value = { id: item.id, name: item.name, parentId: item.parentId }
      // 原始对象的值,有可能存在多次组装，避免多次引用，这里只取第一次的原始值
      if (item.origin) {
        o.origin = this.clone(item.origin)
      } else {
        o.origin = this.clone(item)
      }
      o.undetermined = item.undetermined || false
      o.selected = item.selected || false
      o.disabled = item.disabled || false
      o.loading = item.loading || false
      o.icon = item.icon || ''
      o.tip = item.tip || ''
      o.opened = item.opened || false
    }
    o.id = item.id
    o.parentId = item.parentId
    return o
  },

  // 2.递归循环所有节点,将节点加入到父节点当中
  getChildren (tree, parentId, parentPath) {
    let result = {}
    let child = []
    // 判断子节点是否有被勾选的情况，如有则父节点设置为打开状态
    let hasSelected = false
    tree.forEach((item) => {
      if (item.parentId === parentId) {
        let o = this.assembleItem(item, parentPath)
        if (o.selected) {
          hasSelected = true
        }
        child.push(o)
      }
    })
    child.forEach((item) => {
      let result = this.getChildren(tree, item.id, item.path)
      if (result['child'] && result['child'].length > 0) {
        item.children = result['child']
      }
      item.opened = result['hasSelected']
    })

    // todo 是否需要排序
    result['hasSelected'] = hasSelected
    result['child'] = child
    return result
  },
  /**
     * 对数组对象进行排序操作
     * @param array 需要排序的对象数组
     * @param sort 排序对象{field:'id',sort:'desc'}
     */
  sort (array, sort) {
    array.sort((a, b) => {
      if (sort['sort'] === 'desc') {
        return b[sort['field']] - a[sort['field']]
      } else {
        return b[sort['field']] - a[sort['field']]
      }
    })
  },
  // 克隆一个对象，浅copy
  clone (object) {
    let o = {}
    let keys = Object.keys(object)
    // 不可使用deepExtend，会出现重置无效的问题
    if (keys && keys.length > 0) {
      keys.forEach((k) => {
        // 复制重置后的默认值，防止点击查询时没有使用默认值进行查询
        o[k] = object[k]
      })
    }
    return o
  }

}
